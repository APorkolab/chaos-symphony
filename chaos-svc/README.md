# Chaos Service

This service provides a control plane for injecting failures into the system and managing canary releases.

## Responsibilities

-   **Fault Injection:** Exposes a REST API (`/api/chaos/rules`) to create and delete fault injection rules. These rules are consumed by a `ChaosProducer` interceptor in other services to introduce failures like delays, message duplication, etc.
-   **Canary Release Management:** Exposes a REST API (`/api/canary/config`) to control the traffic splitting for canary releases. It works by calling the `/actuator/env` endpoint on the `orchestrator` service to dynamically change the percentage of traffic routed to the canary consumer.
