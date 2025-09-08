# Chaos Symphony

[![CI Build and Test](https://github.com/APorkolab/chaos-symphony/actions/workflows/ci.yml/badge.svg)](https://github.com/APorkolab/chaos-symphony/actions/workflows/ci.yml)

Chaos Symphony is a fictitious, event-driven, microservices-based system designed to demonstrate and teach advanced software engineering patterns. It simulates a simple payment processing workflow but focuses heavily on resilience, observability, and automated chaos engineering to ensure the system can withstand real-world failures.

This project is not just a demo; it's a hands-on lab. It's built to be broken, observed, and improved. The core philosophy is that a system's true strength is revealed not when it's running perfectly, but when it's gracefully handling failures.

## Service Level Objectives (SLOs)

| Service Level Indicator (SLI) | Objective (per day) |
| :--- | :--- |
| **End-to-End Latency** | `p95(order_processing_time) < 2000ms` |
| **Availability** | `successful_requests / total_requests >= 99.5%` |
| **Data Integrity** | `dlt_messages_total < 0.3% of total messages` |

---



---


## Core Architectural Principles

The design of Chaos Symphony is guided by a set of principles that prioritize robustness and operational maturity over simplistic, "happy-path" implementations.

*   **Event-Driven Architecture (EDA):** The system is built around asynchronous message passing using Apache Kafka. This decouples services, improves scalability, and allows for patterns like the Outbox pattern to ensure data consistency.
*   **Resilience by Design:** We assume failures *will* happen. Services are designed with patterns like Idempotent Consumers, Dead-Letter Queues (DLQs), and automated retries to handle transient and permanent failures gracefully.
*   **Deep Observability:** You can't fix what you can't see. The system is instrumented with Prometheus for metrics, OpenTelemetry for distributed traces, and structured logging. A pre-configured Grafana instance provides a unified view into the system's health, including Service-Level Objective (SLO) dashboards.
*   **Chaos Engineering as a First-Class Citizen:** The system includes a dedicated `chaos-svc` and `gameday-svc` to programmatically inject failures (e.g., latency, errors) and run automated experiments. This allows us to proactively find and fix weaknesses before they impact users.
*   **UI-Facing Service Endpoints:** While not a traditional Backend-for-Frontend (BFF), the architecture provides dedicated API endpoints tailored for UI consumption. For most operations, the UI interacts directly with the relevant service (e.g., `gameday-svc`). For complex, aggregated data views, such as the SLO dashboard, the `streams-analytics` service provides a specific endpoint to simplify data retrieval for the client.

## Key Patterns Implemented

This project serves as a practical example of several critical patterns for building distributed systems.

| Pattern | Implementation | Business Value |
| :--- | :--- | :--- |
| **Saga Pattern** | The payment workflow is orchestrated across the `orchestrator`, `payment-svc`, `inventory-svc`, and `shipping-svc` modules via Kafka messages. | Ensures a long-running business process can complete or be safely compensated across multiple services without using brittle, two-phase commits. |
| **Outbox Pattern** | The `order-api` writes order data and outbound events to its database in a single transaction. A Debezium CDC connector then publishes these events to Kafka. | Guarantees "at-least-once" delivery and prevents the classic "dual-write" problem, ensuring state changes and events are never out of sync. |
| **Idempotent Consumer** | The `payment-svc` tracks processed message IDs in its database. | Prevents duplicate processing of messages, which is critical in "at-least-once" delivery systems. This avoids bugs like double-charging a customer. |
| **Dead-Letter Queue (DLQ)** | The `payment-svc` uses a topic-based DLQ with an exponential backoff retry mechanism, managed by Spring Kafka. Unrecoverable messages are sent to a final `*.dlt` topic. | Protects the system from "poison pill" messages. It isolates failing messages for later analysis and reprocessing without halting the entire system. |
| **Windowed SLO Monitoring** | The `streams-analytics` service uses Kafka Streams to calculate SLO metrics (e.g., p95 latency, error rate) over rolling time windows. | Provides a real-time, actionable view of system health against defined business objectives, enabling proactive incident response. |
| **Automated GameDay** | The `gameday-svc` provides an API to trigger a pre-defined chaos experiment against the `payment-svc` while monitoring the system's SLOs. | Moves chaos engineering from a manual, periodic exercise to an automated, repeatable practice, continuously building confidence in the system's resilience. |

## How to Run the System

**Prerequisites:**
*   Docker and Docker Compose
*   Maven & JDK 21

**1. Build the Project:**
Build all Java modules and the Angular UI from the project root. This command runs all tests (including contract tests when enabled) and prepares the artifacts for Docker.
```bash
mvn -B clean verify
```

**2. Start the Infrastructure & Services:**
Use Docker Compose to build the container images and start the entire system.
```bash
docker-compose up -d --build
```

**3. Accessing the System:**

*   **Angular UI:** [http://localhost:4200](http://localhost:4200) - The main control panel.
*   **Grafana:** [http://localhost:3000](http://localhost:3000) (admin/admin) - View the SLO dashboard.
*   **Prometheus:** [http://localhost:9090](http://localhost:9090)
*   **Kafka UI (Kafdrop):** [http://localhost:9000](http://localhost:9000) - Inspect Kafka topics.

### Deploying to Kubernetes

The project includes a full set of Kubernetes manifests in the `/kubernetes` directory, managed by Kustomize.

**Prerequisites:**
*   A running Kubernetes cluster.
*   `kubectl` configured to connect to your cluster.
*   A container registry (like Docker Hub, GCR, or a private registry) to push your images to.

**Steps:**

1.  **Build and Push Docker Images:**
    The `docker-compose.yml` file can be used to build the images, but you will need to manually tag and push them to your container registry. For example, for the `payment-svc`:
    ```bash
    docker build -t your-registry/payment-svc:latest payment-svc/
    docker push your-registry/payment-svc:latest
    ```
    *(Repeat for all services and the UI)*

2.  **Update Image Names in Deployments:**
    You will need to update the `image` field in each `deployment.yaml` file (e.g., `kubernetes/payment-svc/deployment.yaml`) to point to the images you pushed to your registry.

3.  **Deploy the Application:**
    Apply all the manifests using Kustomize:
    ```bash
    kubectl apply -k kubernetes/
    ```

## 5-Minute Demo Script

This script follows the demonstration flow outlined in the project specification.

1.  **Show Healthy State:**
    *   Open the UI at [http://localhost:4200](http://localhost:4200).
    *   Navigate to the **SLO** view. Point out that all metrics are green: "E2E p95 Latency < 2s", "DLQ Count is 0".

2.  **Create an Order:**
    *   Go to the **Orders** view.
    *   Click "**Create New Order**". An order ID appears in the list below.
    *   Explain that this triggered a SAGA workflow, and the order's timeline can be seen by clicking its ID (future feature).

3.  **Inject Chaos:**
    *   Go to the **Chaos** view.
    *   Enable **Delay** (e.g., 1200ms), **Duplicate**, and **Mutate** toggles.
    *   Create a few more orders.
    *   Switch to the **SLO** view. Show that the p95 latency is rising and the panel is turning red. Show that the DLQ count is also increasing due to mutated, un-parsable messages.

4.  **Drill-Down and Recover:**
    *   Go to the **DLQ** view. You will see the failed messages.
    *   Click the "peek" icon to inspect a message's payload and headers, pointing out the `x-exception-message` header.
    *   Go back to the **Chaos** view and **disable all chaos toggles**.
    *   Return to the **DLQ** view and click "**Replay All**" for the relevant topic.
    *   Show that the DLT count in the SLO view returns to zero.

5.  **Demonstrate Canary Release:**
    *   In the **Chaos** view, enable the **Canary** toggle. Explain that this routes 5% of traffic to a new version of the `payment-svc`.
    *   In Grafana, show the dashboard comparing the performance of `payment-svc` and `payment-svc-canary`.

6.  **Demonstrate Time-Travel Replay:**
    *   In the **Orders** view, click the "**Replay 5m**" button.
    *   Explain that this is resetting the analytics consumer group to re-process the last 5 minutes of events, allowing for "time-travel" analysis in the UI.

7.  **Show Automated GameDay Report:**
    *   Go to the project's GitHub Actions page.
    *   Open the last run of the "**GameDay**" workflow.
    *   Show the downloaded `GameDay-Report.md` artifact, pointing out the measurements before, during, and after the automated chaos experiment.

## Anti-CRUD Checklist

This checklist, derived from the project specification, tracks the implementation of patterns that go beyond simple data-entry applications.

- [x] **SAGA Pattern:** The order workflow is a choreographed Saga across multiple services.
- [x] **Event Schemas:** Versioned schemas are managed by the Confluent Schema Registry.
- [x] **Idempotency:** All consumers use a persistent store to track processed message IDs, preventing duplicates.
- [x] **DLQ Policy:** Implemented with exponential backoff via Spring Kafka's `@RetryableTopic`.
- [x] **Observability:** OTel traces, Prometheus metrics, and a Grafana SLO dashboard are all configured.
- [x] **Testcontainers:** Used in the CI pipeline for integration testing against real dependencies.
- [x] **Contract Tests:** Full Pact implementation with consumer tests in orchestrator and provider verification in payment-svc.
- [x] **RUNBOOK:** A detailed `RUNBOOK.md` exists for common operational scenarios.
- [x] **Automated GameDay:** A GitHub Actions workflow automates chaos experiments and reporting.
- [x] **Replay Capability:** The system can replay the last 5 minutes of events for analysis.
