## `order-api`
```markdown
# order-api
Indítja a rendelést (`/api/orders/start`).  
Port: 8081 (local profil).

### Endpoints
- `POST /api/orders/start?amount=123` -> generál orderId
- `POST /api/orders/{orderId}/start?amount=123`

### Metrikák
- `/actuator/prometheus` (orders.started)

### CORS demo
Nyitott CORS (Local UI-hoz), lásd `OrderApiCorsConfig.java`.
