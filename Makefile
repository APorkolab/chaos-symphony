.PHONY: up down run stop e2e chaos-on chaos-off clean

up:
\tcd deployment && docker compose up -d

down:
\tcd deployment && docker compose down -v

run:
\tmvn -q -pl order-api,orchestrator,payment-svc,inventory-svc,shipping-svc spring-boot:run -Dspring-boot.run.profiles=local

stop:
\tpkill -f 'org.springframework.boot.loader.JarLauncher' || true

e2e:
\tbash scripts/batch-start.sh

chaos-on:
\tcp chaos/rules.json chaos/rules.active.json

chaos-off:
\trm -f chaos/rules.active.json

clean:
\tmvn -q clean
