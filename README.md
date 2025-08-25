# Chaos Symphony

[![Build](https://img.shields.io/github/actions/workflow/status/APorkolab/chaos-symphony/ci.yml?branch=main)](../../actions)
[![License](https://img.shields.io/badge/license-MIT-informational.svg)](LICENSE)
[![Issues](https://img.shields.io/github/issues/APorkolab/chaos-symphony.svg)](../../issues)



# Chaos Symphony – Outbox EventRouter


README.md (rövid váz):

# Chaos Symphony

## Run
```bash
cd deployment && docker compose up -d
mvn -q -DskipTests install
mvn -pl orchestrator,payment-svc,inventory-svc,shipping-svc,order-api,streams-analytics,dlq-admin spring-boot:run


# start order
curl -s -X POST "http://localhost:8080/api/orders/start?amount=42"
# analytics
curl -s http://localhost:8087/api/metrics/paymentStatus
# DLQ list
curl -s http://localhost:8086/api/dlq/topics


Break & Recover

    Break: set chaos rules (failRate) at http://localhost:<chaos-svc>/chaos.html

    See DLT in Kafdrop (inventory.requested.DLT)

    Replay:


curl -H "X-Admin-Token: secret123" -X POST http://localhost:8086/api/dlq/inventory.requested.DLT/replay

Event-driven demo (Java 21, Spring Boot 3.4, Kafka) – Payment → Inventory → Shipping, orchestration, Chaos injection, DLT replay, Streams analytics.

## Indítás 3 lépésben
```bash
cd deployment
docker compose up -d
mvn -q -pl "order-api,payment-svc,inventory-svc,shipping-svc,orchestrator,dlq-admin,streams-analytics" spring-boot:run
./scripts/load.sh 50

```

# Architektúra

flowchart LR
  A[order-api] -- payment.requested --> P[payment-svc]
  P -- payment.result --> O[orchestrator]
  O -- inventory.requested --> I[inventory-svc]
  I -- inventory.result --> O
  O -- shipping.requested --> S[shipping-svc]
  S -- shipping.result --> O
  O --> AN[streams-analytics]
  classDef svc fill:#0f172a,stroke:#94a3b8,color:#e2e8f0;
  class A,P,O,I,S,AN svc


# Metrikák

Grafana – import

Grafana → Connections → Data sources → add Prometheus (URL pl. http://localhost:9090).

Grafana → Dashboards → New → Import → Paste JSON (lásd lent) → válaszd ki a Prometheus data source-ot → Import.

# Postman collection

Deploy könyvtárban megtalálható a Postman kollekció és environment fájl:

ChaosSvc.postman_collection.json

ChaosSvc.local_environment.json


Használat:

Importáld a kollekciót és az environmentet Postmanbe.

Válaszd ki az environmentet: Chaos Symphony - Local.

Futtatható endpointok:

GET {{baseUrl}}/api/healthz

GET {{baseUrl}}/api/chaos/status

GET {{baseUrl}}/api/chaos/rules

GET {{baseUrl}}/api/chaos/rules/:topic

POST {{baseUrl}}/api/chaos/rules (bulk set – minta body benne)

PUT {{baseUrl}}/api/chaos/rules/payment.result

DELETE {{baseUrl}}/api/chaos/rules/payment.result

DELETE {{baseUrl}}/api/chaos/rules

Külön kollekciók az orchestrator és a DLQ-admin végpontokhoz is, plusz egy „Smoke Test” Postman Runner futást 20 rendelésre:

Order + Orchestrator kollekció:
ChaosSymphony.order_orchestrator.postman_collection.json

DLQ Admin kollekció:
ChaosSymphony.dlq_admin.postman_collection.json

Services environment (base URL-ok):
ChaosSymphony.services_environment.json

Mit tudnak?

Order & Orchestrator:

Start NEW order (random UUID) – pre-request script generál orderId-t, amount változóval.

Start order with explicit orderId

Orchestrator egészség és loggers actuator hívások.

DLQ Admin:

List DLQ topics – első találatot beírja dltTopic változóba.

Replay chosen DLT topic – a fent beállított dltTopic-kal.

Használat:

Importáld a két kollekciót és az environment fájlt.

Válaszd az environmentet: Chaos Symphony - Services.

Futtasd az Order API / Start NEW order (random UUID)-t 10–50 alkalommal Postman Runnerrel (iterations: 20–50).

Amountot az environmentben állíthatod.

Ha akarsz DLQ-t provokálni, kapcsolj be chaos szabályt payment/inventory/shipping resultokra, majd Replay chosen DLT topic.


# Service portok

order-api: 8080

payment-svc: 8081

inventory-svc: 8084

shipping-svc: 8085

orchestrator: 8091

dlq-admin: 8089

streams-analytics: 8095

Kafdrop: 9000 (http://localhost:9000)

Kafka topikok

payment.requested → payment.result

inventory.requested → inventory.result

shipping.requested → shipping.result

DLT: *.DLT

Analytics: analytics.payment.status.count

# Gyors API-k


## rendelés indítás
curl -X POST "http://localhost:8080/api/orders/start?amount=100"

## DLQ lista + replay
curl -s http://localhost:8089/api/dlq/topics
curl -X POST http://localhost:8089/api/dlq/payment.result.DLT/replay

## Streams metrics (ha bekapcsoltad a REST-et)
curl -s http://localhost:8095/api/metrics/paymentStatus

# Chaos szabályok (példa)

curl -X POST http://localhost:8085/api/chaos/rules \
  -H "Content-Type: application/json" \
  -d '{"topic:payment.result":{"pDrop":0.15,"pDup":0.05,"maxDelayMs":300,"pCorrupt":0.02}}'


# Screenshotok

Kafdrop – topicok / üzenetfolyam

Grafana – metrikák

# asd

---

# A) Postgres **Dockerben**

> Feltételezés: a konténer neve `postgres` és a superuser `postgres`. Ha más, cseréld a parancsokban.

## 0) Belépés a psql-be

```bash
docker exec -it postgres psql -U postgres
```

## 1) Felhasználók, adatbázis

psql-ben:

```sql
-- app felhasználó (az alkalmazásodnak)
DO $$
BEGIN
   IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'app') THEN
      CREATE ROLE app WITH LOGIN PASSWORD 'pass';
   END IF;
END$$;

-- debezium felhasználó (REPLICATION joggal)
DO $$
BEGIN
   IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'debezium') THEN
      CREATE ROLE debezium WITH LOGIN REPLICATION PASSWORD 'dbz';
   END IF;
END$$;

-- orders adatbázis
DO $$
BEGIN
   IF NOT EXISTS (SELECT FROM pg_database WHERE datname = 'orders') THEN
      CREATE DATABASE orders OWNER app;
   END IF;
END$$;
```

## 2) Kapcsolódás az `orders` DB-hez és séma/jogosultságok

```sql
\c orders

-- séma használat az app-nak
GRANT USAGE ON SCHEMA public TO app;

-- későbbi CREATE jog (opcionális)
GRANT CREATE ON SCHEMA public TO app;
```

## 3) Outbox tábla (CDC-hez)

```sql
-- outbox tábla (alap)
CREATE TABLE IF NOT EXISTS public.order_outbox (
  id            UUID PRIMARY KEY,
  aggregate_id  UUID        NOT NULL,
  occurred_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
  payload       TEXT        NOT NULL,
  published     BOOLEAN     NOT NULL DEFAULT false,
  type          VARCHAR(255) NOT NULL
);

-- opcionális: SMT-hez hasznos mezők
ALTER TABLE public.order_outbox
  ADD COLUMN IF NOT EXISTS aggregate_type VARCHAR(255) NOT NULL DEFAULT 'Order';

-- (ha saját millis timestampet szeretnél)
ALTER TABLE public.order_outbox
  ADD COLUMN IF NOT EXISTS occurred_at_ms BIGINT;
```

## 4) Alkalmazás és Debezium jogosultságok

```sql
-- app: R/W az outboxra
GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE public.order_outbox TO app;

-- debezium: olvasás (CDC)
GRANT SELECT ON TABLE public.order_outbox TO debezium;
```

## 5) Logical decoding bekapcsolása

> **Újraindítás kell** a `wal_level` miatt.

**Lehetőség 1 – ALTER SYSTEM (kényelmes):**

```sql
ALTER SYSTEM SET wal_level = 'logical';
ALTER SYSTEM SET max_wal_senders = 10;
ALTER SYSTEM SET max_replication_slots = 10;
```

Kilépés:

```sql
\q
```

Konténer restart:

```bash
docker restart postgres
```

**Ellenőrzés:**

```bash
docker exec -it postgres psql -U postgres -d orders -c "SHOW wal_level;"
# "logical" kell legyen
```

## 6) Publikáció létrehozása (Debeziumnak)

```bash
docker exec -it postgres psql -U postgres -d orders -c "DROP PUBLICATION IF EXISTS dbz_publication;"
docker exec -it postgres psql -U postgres -d orders -c "CREATE PUBLICATION dbz_publication FOR TABLE public.order_outbox;"
docker exec -it postgres psql -U postgres -d orders -c "\dRp+"
```

## 7) Replikációs slotok (diagnosztika)

```bash
docker exec -it postgres psql -U postgres -d orders -c "SELECT slot_name, plugin, active, confirmed_flush_lsn FROM pg_replication_slots;"
# törlés, ha szükséges:
docker exec -it postgres psql -U postgres -d orders -c "SELECT pg_drop_replication_slot('orders_slot');"
```

---

# B) **Helyi** Postgres (localhost)

## 0) Belépés a psql-be

```bash
# ha van 'postgres' superuser:
psql -h localhost -p 5432 -U postgres
# ha Postgres.app: lehet, hogy user nélkül megy:
# psql -h localhost -p 5432
```

## 1) Felhasználók, adatbázis

```sql
DO $$
BEGIN
   IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'app') THEN
      CREATE ROLE app WITH LOGIN PASSWORD 'pass';
   END IF;
END$$;

DO $$
BEGIN
   IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'debezium') THEN
      CREATE ROLE debezium WITH LOGIN REPLICATION PASSWORD 'dbz';
   END IF;
END$$;

DO $$
BEGIN
   IF NOT EXISTS (SELECT FROM pg_database WHERE datname = 'orders') THEN
      CREATE DATABASE orders OWNER app;
   END IF;
END$$;
```

## 2) Kapcsolódás / jogosultságok / outbox tábla

```sql
\c orders

GRANT USAGE ON SCHEMA public TO app;
GRANT CREATE ON SCHEMA public TO app;

CREATE TABLE IF NOT EXISTS public.order_outbox (
  id            UUID PRIMARY KEY,
  aggregate_id  UUID        NOT NULL,
  occurred_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
  payload       TEXT        NOT NULL,
  published     BOOLEAN     NOT NULL DEFAULT false,
  type          VARCHAR(255) NOT NULL
);

ALTER TABLE public.order_outbox
  ADD COLUMN IF NOT EXISTS aggregate_type VARCHAR(255) NOT NULL DEFAULT 'Order';

-- opcionális millis timestamp
ALTER TABLE public.order_outbox
  ADD COLUMN IF NOT EXISTS occurred_at_ms BIGINT;

GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE public.order_outbox TO app;
GRANT SELECT ON TABLE public.order_outbox TO debezium;
```

## 3) Logical decoding bekapcsolása (helyi)

```sql
SHOW config_file;  -- jegyezd meg az elérési utat
```

**Gyors módszer (ALTER SYSTEM):**

```sql
ALTER SYSTEM SET wal_level = 'logical';
ALTER SYSTEM SET max_wal_senders = 10;
ALTER SYSTEM SET max_replication_slots = 10;
\q
```

**Restart szükséges:**

* **Postgres.app**: a GUI-ból Stop → Start.
* **Homebrew**:

  ```bash
  brew services restart postgresql@16   # verziót cseréld a telepítettedre
  ```

**Ellenőrzés:**

```bash
psql -h localhost -p 5432 -d orders -c "SHOW wal_level;"
```

## 4) Publikáció

```bash
psql -h localhost -p 5432 -d orders -U postgres -c "DROP PUBLICATION IF EXISTS dbz_publication;"
psql -h localhost -p 5432 -d orders -U postgres -c "CREATE PUBLICATION dbz_publication FOR TABLE public.order_outbox;"
psql -h localhost -p 5432 -d orders -U postgres -c "\dRp+"
```

## 5) Slotok (diagnosztika)

```bash
psql -h localhost -p 5432 -d orders -c "SELECT slot_name, plugin, active, confirmed_flush_lsn FROM pg_replication_slots;"
psql -h localhost -p 5432 -d orders -c "SELECT pg_drop_replication_slot('orders_slot');"
```

---

## Gyors ellenőrző parancsok (mindkét környezetre)

**Outbox top 10:**

```sql
SELECT id, aggregate_id, type, published, occurred_at
FROM public.order_outbox
ORDER BY occurred_at DESC
LIMIT 10;
```

**Felhasználók:**

```sql
\du
\du app
\du debezium
```

**Táblák/jogok:**

```sql
\d+ public.order_outbox
```

**Publikációk:**

```sql
\dRp+
```

**Aktív kapcsolat info (hasznos Debeziumhoz):**

```sql
SELECT pid, usename, application_name, state, backend_start
FROM pg_stat_activity
ORDER BY backend_start DESC
LIMIT 10;
```

---

## Mit NE felejts:

* Debezium Postgres connectornak a **host.docker.internal:5432** hostot add (ha Postgres a hoston fut), és `publication.name=dbz_publication`, `publication.autocreate.mode=disabled`.
* Ha az Outbox EventRouter-t használod: legyen oszlop **`aggregate_type`** (vagy vedd ki a mappinget), a **timestamp** mezőt pedig vagy hagyd ki (Debezium `source.ts_ms`-t használ), vagy adj **`occurred_at_ms BIGINT`**-et.
* App user (pl. `app`) kapjon **SELECT/INSERT/UPDATE/DELETE** jogot az outbox táblára.
* Debezium user (pl. `debezium`) kapjon **SELECT** jogot az outbox táblára.
Nagyon jó, hogy kérdezed, mert a Postgres Debezium-CDC beüzemeléséhez nem elég a felhasználó/db/outbox tábla. Van még **egy rakás „egyéb parancs”**, amit mindenképpen le kell futtatni — külön a Postgres oldalon, külön a Debezium/Kafka oldalon.

---

# Egyéb parancsok
## 🔹 Postgres oldali parancsok

👉 Ezeket **egyszer** kell megcsinálni (superuser joggal).

### 1. Replication jogok, WAL beállítás

```sql
-- Debezium usernek kell replication jog
ALTER ROLE debezium WITH REPLICATION;

-- Ellenőrzés
\du debezium
```

```sql
-- WAL szint ellenőrzése
SHOW wal_level;  -- "logical" kell legyen

-- ha nem az, akkor:
ALTER SYSTEM SET wal_level = 'logical';
ALTER SYSTEM SET max_wal_senders = 10;
ALTER SYSTEM SET max_replication_slots = 10;
```

➡ Restart Postgres szükséges.

---

### 2. Jogosultságok az `orders` DB-hez

```sql
GRANT CONNECT ON DATABASE orders TO debezium;
GRANT CONNECT ON DATABASE orders TO app;
```

---

### 3. Jogosultságok a sémára és táblákra

```sql
GRANT USAGE ON SCHEMA public TO debezium;
GRANT USAGE ON SCHEMA public TO app;

GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO app;
GRANT SELECT ON ALL TABLES IN SCHEMA public TO debezium;
```

---

### 4. Jövőbeli objektumokra jog

```sql
ALTER DEFAULT PRIVILEGES IN SCHEMA public
GRANT SELECT ON TABLES TO debezium;

ALTER DEFAULT PRIVILEGES IN SCHEMA public
GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO app;
```

---

### 5. Publikáció létrehozása

```sql
DROP PUBLICATION IF EXISTS dbz_publication;
CREATE PUBLICATION dbz_publication FOR TABLE public.order_outbox;
create index if not exists idx_order_outbox_aggregate on order_outbox (aggregate_id);
create index if not exists idx_order_outbox_type_occurred on order_outbox (type, occurred_at);

```

Ellenőrzés:

```sql
\dRp+
```

---

# 🔹 Debezium oldali parancsok

👉 Ezeket **curl-lal** küldöd a Kafka Connect REST API-nak.

### 1. Connector létrehozás / update

```bash
curl -s -X PUT http://localhost:8083/connectors/orders-connector/config \
  -H "Content-Type: application/json" \
  -d '{
    "connector.class": "io.debezium.connector.postgresql.PostgresConnector",
    "database.hostname": "host.docker.internal",
    "database.port": "5432",
    "database.user": "debezium",
    "database.password": "dbz",
    "database.dbname": "orders",
    "topic.prefix": "ordersdb",
    "slot.name": "orders_slot",
    "plugin.name": "pgoutput",
    "publication.name": "dbz_publication",
    "publication.autocreate.mode": "disabled",
    "schema.include.list": "public",
    "table.include.list": "public.order_outbox",
    "tombstones.on.delete": "false",
    "transforms": "outbox",
    "transforms.outbox.type": "io.debezium.transforms.outbox.EventRouter",
    "transforms.outbox.route.topic.replacement": "orders.$r",
    "transforms.outbox.table.field.event.id": "id",
    "transforms.outbox.table.field.event.key": "aggregate_id",
    "transforms.outbox.table.field.event.type": "type",
    "transforms.outbox.table.field.payload": "payload",
    "transforms.outbox.table.fields.additional.placement": "type:header:eventType,aggregate_id:header:aggregateId",
    "transforms.outbox.table.expand.headers": "true",
    "key.converter": "org.apache.kafka.connect.json.JsonConverter",
    "value.converter": "org.apache.kafka.connect.json.JsonConverter",
    "key.converter.schemas.enable": "false",
    "value.converter.schemas.enable": "false"
  }'
```

### 2. Connector újraindítás

```bash
curl -s -X POST http://localhost:8083/connectors/orders-connector/restart
```

### 3. Státusz lekérdezés

```bash
curl -s http://localhost:8083/connectors/orders-connector/status | jq .
```

---

# 🔹 Kafka oldali parancsok

👉 Ezekkel ellenőrzöd, hogy jön-e az outbox event.

### 1. Utolsó üzenet olvasása (kcat-tel)

```bash
kcat -b localhost:9092 -t orders.OrderCreated -C -o -1 -q -J | jq .
```

Ha Debezium alap topicot ad:

```bash
kcat -b localhost:9092 -t ordersdb.public.order_outbox -C -o -1 -q -J | jq .
```

---

# 🔹 Összefoglaló sorrend

1. **Postgres oldalon:**

   * replication jog + WAL `logical`
   * `app` és `debezium` user létrehozása
   * `orders` DB + `order_outbox` tábla
   * jogok kiosztása (SELECT/INSERT stb.)
   * publikáció létrehozása

2. **Debezium oldalon:**

   * connector config PUT
   * restart
   * státusz ellenőrzés

3. **Kafka oldalon:**

   * topic figyelés kcat-tel

---

Helyi szkript

Egy egyben futtatható script, ami végigviszi az egész Postgres → Debezium → Kafka beállítást.
Egyetlen fájl, két mód: docker (Postgres konténerben) és local (helyi Postgres).

- mentsd el pl. bootstrap_cdc.sh néven, 
- majd:
chmod +x bootstrap_cdc.sh
- ./bootstrap_cdc.sh docker   # ha Postgres konténerben fut (docker exec)
- ./bootstrap_cdc.sh local    # ha helyi Postgres (Postgres.app / Homebrew)


Mit csinál a script?

létrehozza az app és debezium usereket, az orders adatbázist,

felhúzza az order_outbox táblát + aggregate_type oszlopot,

beállítja a WAL-t logical-ra (dockerben automatikus restart, localnál jelzi, hogy indítsd újra),

létrehozza/újraépíti a dbz_publication-t az outbox táblára,

felkonfigurálja a Debezium Postgres connectort Outbox SMT-vel (timestamp nélkül, így nem buksz az INT64-en),

kiírja a connector státuszát és a replikációs slot állapotát.



 #  README-hez 3 parancs

```bash

docker compose up -d                                  # infra
mvn -q -pl "order-api,payment-svc,inventory-svc,shipping-svc,orchestrator,dlq-admin,streams-analytics" spring-boot:run
./scripts/load.sh 100
```

DLQ replay példa:

```bash
   curl -s http://localhost:8089/api/dlq/topics
   curl -X POST http://localhost:8089/api/dlq/payment.result.DLT/replay

```



Chaos szabály példa (ha bekötöd a chaos-svc-t REST-tel):

```bash
curl -X POST http://localhost:8085/api/chaos/rules \
  -H "Content-Type: application/json" \
  -d '{"topic:payment.result":{"pDrop":0.15,"pDup":0.05,"maxDelayMs":300,"pCorrupt":0.02}}'
```