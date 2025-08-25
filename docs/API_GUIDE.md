# API Usage and Postman Guide

This guide provides detailed instructions for interacting with the Chaos Symphony services using Postman or cURL. It's your primary reference for testing, debugging, and demonstrating the system's features.

## 📮 Using Postman for Automated Testing

For a streamlined experience, Postman collections and environments are included in the `deployment/` directory. They are the recommended way to interact with the system for the first time.

* **Collections:**
    * `ChaosSymphony.order_orchestrator.postman_collection.json`
    * `ChaosSymphony.dlq_admin.postman_collection.json`
    * `ChaosSvc.postman_collection.json`
* **Environments:**
    * `ChaosSymphony.services_environment.json` (contains base URLs for all services)

### How to Use

1.  **Import** the three collection files and the environment file into Postman.
2.  From the Environments tab, select **Chaos Symphony - Services** as your active environment.
3.  You can now use the pre-configured requests to interact with the services.

### Simulating Load

To simulate multiple orders, use the **Postman Runner** on the `Order & Orchestrator` collection. Select the `Start NEW order (random UUID)` request and set the number of iterations (e.g., 20-50) to generate load on the system.

---

## 👨‍💻 Using cURL from the Command Line

Here are the essential cURL commands for demonstrating the core functionalities of the system.

### `order-api` (Port `8080`)

This is the main entry point for starting the Saga workflow.

```bash
# Start a normal "happy path" order
curl -X POST "http://localhost:8080/api/orders/start?amount=42"

# Start an order designed to fail at the inventory step
# The chaos-svc is pre-configured to recognize this specific orderId
curl -X POST "http://localhost:8080/api/orders/start?amount=10&orderId=BREAK-ME"

# API Usage and Postman Guide

This guide provides detailed instructions for interacting with the Chaos Symphony services using Postman or cURL. It's your primary reference for testing, debugging, and demonstrating the system's features.

## 📮 Using Postman for Automated Testing

For a streamlined experience, Postman collections and environments are included in the `deployment/` directory. They are the recommended way to interact with the system for the first time.

* **Collections:**
    * `ChaosSymphony.order_orchestrator.postman_collection.json`
    * `ChaosSymphony.dlq_admin.postman_collection.json`
    * `ChaosSvc.postman_collection.json`
* **Environments:**
    * `ChaosSymphony.services_environment.json` (contains base URLs for all services)

### How to Use

1.  **Import** the three collection files and the environment file into Postman.
2.  From the Environments tab, select **Chaos Symphony - Services** as your active environment.
3.  You can now use the pre-configured requests to interact with the services.

### Simulating Load

To simulate multiple orders, use the **Postman Runner** on the `Order & Orchestrator` collection. Select the `Start NEW order (random UUID)` request and set the number of iterations (e.g., 20-50) to generate load on the system.

---

## 👨‍💻 Using cURL from the Command Line

Here are the essential cURL commands for demonstrating the core functionalities of the system.

### `order-api` (Port `8080`)

This is the main entry point for starting the Saga workflow.

```bash
# Start a normal "happy path" order
curl -X POST "http://localhost:8080/api/orders/start?amount=42"

# Start an order designed to fail at the inventory step
# The chaos-svc is pre-configured to recognize this specific orderId
curl -X POST "http://localhost:8080/api/orders/start?amount=10&orderId=BREAK-ME"

```

### `chaos-svc` (Port `8085`)

This service allows you to define failure rules for any topic at runtime.



```
# Set a rule for the 'payment.result' topic
# Introduces a 15% drop chance, 5% duplication chance, and max 300ms delay.
curl -X POST http://localhost:8085/api/chaos/rules \
  -H "Content-Type: application/json" \
  -d '{
    "topic:payment.result": {
      "pDrop": 0.15,
      "pDup": 0.05,
      "maxDelayMs": 300,
      "pCorrupt": 0.02
    }
  }'

# Get the status of all active chaos rules
curl -s http://localhost:8085/api/chaos/rules | jq .

# Delete the rule for a specific topic
curl -X DELETE http://localhost:8085/api/chaos/rules/payment.result

# Delete all active chaos rules
curl -X DELETE http://localhost:8085/api/chaos/rules

```

### `dlq-admin` (Port `8089`)

This service lets you inspect and manage Dead-Letter Topics (DLTs).



```
# List all topics that currently have messages in their DLT
curl -s http://localhost:8089/api/dlq/topics | jq .

# Peek at the first 5 messages in the inventory DLT
# Replace 'inventory.requested.DLT' with a topic from the list above.
curl -s "http://localhost:8089/api/dlq/inventory.requested.DLT/peek?n=5" | jq .

# Replay all messages from a specific DLT to re-process them
curl -X POST http://localhost:8089/api/dlq/inventory.requested.DLT/replay

```

### `streams-analytics` (Port `8095`)

This service exposes real-time metrics calculated by Kafka Streams.



```
# Get the counts of different payment statuses
curl -s http://localhost:8095/api/metrics/paymentStatus | jq .

```

