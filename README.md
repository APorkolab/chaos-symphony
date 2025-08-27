# Chaos Symphony

Chaos Symphony is a fictitious, event-driven, microservices-based system designed to demonstrate and teach advanced software engineering patterns. It simulates a simple payment processing workflow but focuses heavily on resilience, observability, and automated chaos engineering to ensure the system can withstand real-world failures.

This project is not just a demo; it's a hands-on lab. It's built to be broken, observed, and improved. The core philosophy is that a system's true strength is revealed not when it's running perfectly, but when it's gracefully handling failures.

## Core Architectural Principles

The design of Chaos Symphony is guided by a set of principles that prioritize robustness and operational maturity over simplistic, "happy-path" implementations.

*   **Event-Driven Architecture (EDA):** The system is built around asynchronous message passing using Apache Kafka. This decouples services, improves scalability, and allows for patterns like the Outbox pattern to ensure data consistency.
*   **Resilience by Design:** We assume failures *will* happen. Services are designed with patterns like Idempotent Consumers, Dead-Letter Queues (DLQs), and automated retries to handle transient and permanent failures gracefully.
*   **Deep Observability:** You can't fix what you can't see. The system is instrumented with Prometheus for metrics, Loki for logs, and Tempo for traces. A pre-configured Grafana instance provides a unified view into the system's health, including Service-Level Objective (SLO) dashboards.
*   **Chaos Engineering as a First-Class Citizen:** The system includes a dedicated `chaos-svc` and `gameday-svc` to programmatically inject failures (e.g., latency, errors) and run automated experiments. This allows us to proactively find and fix weaknesses before they impact users.
*   **UI-Facing Service Endpoints:** While not a traditional Backend-for-Frontend (BFF), the architecture provides dedicated API endpoints tailored for UI consumption. For most operations, the UI interacts directly with the relevant service (e.g., `gameday-svc`). For complex, aggregated data views, such as the SLO dashboard, the `streams-analytics` service provides a specific endpoint to simplify data retrieval for the client.

## Key Patterns Implemented

This project serves as a practical example of several critical patterns for building distributed systems.

| Pattern | Implementation | Business Value |
| :--- | :--- | :--- |
| **Saga Pattern** | The payment workflow is orchestrated across the `orchestrator`, `payment-svc`, `inventory-svc`, and `shipping-svc` modules via Kafka messages. | Ensures a long-running business process can complete or be safely compensated across multiple services without using brittle, two-phase commits. |
| **Outbox Pattern** | The `orchestrator` writes business data and outbound events to its database in a single transaction. A Debezium CDC connector then publishes these events to Kafka. | Guarantees "at-least-once" delivery and prevents the classic "dual-write" problem, ensuring state changes and events are never out of sync. |
| **Idempotent Consumer** | The `payment-svc` tracks processed message IDs in its database. | Prevents duplicate processing of messages, which is critical in "at-least-once" delivery systems. This avoids bugs like double-charging a customer. |
| **Dead-Letter Queue (DLQ)** | The `payment-svc` uses a topic-based DLQ with an exponential backoff retry mechanism, managed by Spring Kafka. Unrecoverable messages are sent to a final `*.dlt` topic. | Protects the system from "poison pill" messages. It isolates failing messages for later analysis and reprocessing without halting the entire system. |
| **Windowed SLO Monitoring** | The `streams-analytics` service uses Kafka Streams to calculate SLO metrics (e.g., p95 latency, error rate) over rolling time windows. | Provides a real-time, actionable view of system health against defined business objectives, enabling proactive incident response. |
| **Automated GameDay** | The `gameday-svc` provides an API to trigger a pre-defined chaos experiment against the `payment-svc` while monitoring the system's SLOs. | Moves chaos engineering from a manual, periodic exercise to an automated, repeatable practice, continuously building confidence in the system's resilience. |

## Running the System

**Prerequisites:**
*   Docker and Docker Compose
*   Maven
*   Java 17

**1. Build the Project:**
First, build all the Java modules using Maven. This will also run the Pact contract tests.
```bash
./mvnw clean install
```

**2. Start the Infrastructure & Services:**
Use Docker Compose to bring up the entire system. This includes the application services, Kafka, databases, and the observability stack.
```bash
docker-compose up -d
```

**3. Accessing the System:**

*   **Angular UI:** [http://localhost:4200](http://localhost:4200)
    *   This is the main interface for interacting with the system.
*   **Grafana:** [http://localhost:3000](http://localhost:3000) (admin/admin)
    *   Explore the pre-built "Chaos Symphony SLO" dashboard.
*   **Prometheus:** [http://localhost:9090](http://localhost:9090)
*   **Kafka UI (Kafdrop):** [http://localhost:9000](http://localhost:9000)

## How to Demonstrate the Features

This section provides a script for a live demonstration of the system's capabilities.

#### Demo 1: The "Happy Path" Workflow

1.  **Open the UI:** Navigate to [http://localhost:4200](http://localhost:4200).
2.  **Start a Workflow:** In the "Orchestrator" panel, enter a unique `correlationId` (e.g., `test-1`) and a payload (e.g., `{"amount": 100}`). Click "Start Workflow".
3.  **Observe:** Watch the logs in the UI. You'll see the `orchestrator` start the process and the `payment-svc` process the payment.
4.  **Check Kafka:** Open Kafdrop ([http://localhost:9000](http://localhost:9000)) to see the messages flowing through the `payment.requested` and other topics.

#### Demo 2: Failure, DLT, and Manual Recovery

1.  **Inject a Failure:** In the UI's "Payment Service" panel, use the "Set Response Type" feature to make the service return an `ERROR`.
2.  **Start a New Workflow:** Go back to the "Orchestrator" panel and start a new workflow with a different `correlationId` (e.g., `test-fail-1`).
3.  **Observe the Failure:** The `payment-svc` will fail to process the message. After a few automated retries (configured with exponential backoff), Spring Kafka will give up and send the message to the `payment.requested.dlt` topic.
4.  **View the DLT:** In the UI, navigate to the "Dead Letter Queue" view. You will see the failed message here.
5.  **Fix the System:** Go back to the "Payment Service" panel and set its response type back to `SUCCESS`.
6.  **Recover:** In the "Dead Letter Queue" view, click "Replay All". This will move the message from the DLT back to the main topic. The `payment-svc` will now successfully process it, and the workflow will complete.

#### Demo 3: Automated GameDay and SLO Monitoring

1.  **Open Grafana:** Open the "Chaos Symphony SLO" dashboard in Grafana ([http://localhost:3000](http://localhost:3000)). You should see green SLOs.
2.  **Start the GameDay:** In the UI's "GameDay Service" panel, click "Start Experiment".
3.  **Observe the Chaos:** The `gameday-svc` will call the `chaos-svc` to inject latency into the `payment-svc`.
4.  **Watch the SLOs:** Switch back to Grafana. You will see the `p95 latency` metric increase. As it crosses the SLO threshold, the panel will turn red. The "SLO Burn Rate" panel will also start to increase, showing you how quickly you are consuming your error budget.
5.  **View Live SLOs in UI:** The "SLO Status" panel in the main UI is powered by a BFF endpoint and will also reflect the degraded performance in real-time.
6.  **Automatic Resolution:** The chaos experiment is time-boxed. After it ends, the latency will return to normal, and the SLOs in Grafana will turn green again. This demonstrates the system's ability to self-heal after a transient failure.

## Anti-CRUD Checklist

This checklist tracks the project's progress in moving beyond simple CRUD operations to a more robust, message-driven architecture.

- [x] The system is event-driven.
- [x] At least one service uses the Outbox pattern. (`orchestrator`)
- [x] At least one service has an Idempotent Consumer. (`payment-svc`)
- [x] The system uses a message broker (Kafka).
- [x] The system includes consumer-driven contract tests (Pact).
- [x] The system is observable (logs, metrics, traces).
- [x] The system has defined SLOs.
- [x] The system includes a automated chaos engineering experiment.
- [ ] The project has a `RUNBOOK.md`.
- [x] The system can be deployed and run with a single command (`docker-compose up`).
- [ ] The system has end-to-end tests that validate a full business workflow.
- [ ] The UI is served by a Backend-for-Frontend (BFF).
- [x] The system includes a Dead-Letter Queue mechanism.
- [x] The DLT mechanism includes an automated retry policy (e.g., exponential backoff).
- [x] The system allows for manual DLT reprocessing.
- [x] The system has a dedicated UI for operational tasks (like DLT management).