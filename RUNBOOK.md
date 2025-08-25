# Chaos Symphony - Runbook

This document provides step-by-step instructions for handling common operational issues in the Chaos Symphony system.

---

### Scenario 1: DLQ Count is Rising Rapidly

**Symptom:** The "DLT Count" panel in Grafana shows a sustained increase in messages. The `dlt_messages_total` metric is growing.

**Triage & Resolution:**

1.  **Assess the Impact:**
    *   Navigate to the **DLQ** view in the application UI (`http://localhost:4200/dlq`).
    *   Select the topic with the rising message count to inspect the failed messages.
    *   Examine the headers and payload of a few messages. Look for a common root cause in the `x-exception-message` header (e.g., `ConnectException`, `NullPointerException`, a specific business error).

2.  **Attempt Initial Recovery:**
    *   If the error seems transient (e.g., a temporary network issue), use the **"Retry All"** button in the DLQ UI for the affected topic.
    *   Monitor the DLQ count in Grafana. If the messages are processed successfully and the count drops, the issue was likely transient.

3.  **Isolate the Fault (If Retry Fails):**
    *   If messages fail again and land back in the DLQ, there is likely a persistent bug or misconfiguration.
    *   **If a Canary deployment is active, immediately turn it off** via the Chaos Panel in the UI to halt the impact on the new version.
    *   Notify the development team responsible for the failing service (e.g., `payment-svc` if messages are in `payment.requested.DLT`), providing them with sample message payloads and exception details from the DLQ UI.

---

### Scenario 2: p95 Latency Exceeds SLO (> 2 seconds)

**Symptom:** The "Order E2E p95 Latency" panel in Grafana is consistently above the 2000ms SLO threshold. The `orders_slo_burn_rate` is greater than 1.

**Triage & Resolution:**

1.  **Stop the Bleeding:**
    *   Navigate to the **Chaos Panel** in the UI (`http://localhost:4200/chaos`).
    *   **Immediately disable all active chaos experiments** (especially `DELAY`). This is the most likely cause of artificial latency.
    *   Observe the p95 latency graph for the next 2-3 minutes. If it returns to normal, the chaos experiment was the cause.

2.  **Investigate Real Performance Issues (If Chaos Was Off):**
    *   If latency remains high, check the **Kafka Consumer Lag** panel in Grafana. A high and rising lag on a particular topic indicates that the consumer service for that topic is overwhelmed or stuck.
    *   Check the logs of the struggling service (e.g., `docker-compose logs -f payment-svc`) for errors, exceptions, or long-running query warnings.

3.  **Scale to Mitigate:**
    *   As a temporary mitigation, you can scale the number of consumer instances for the affected service.
    *   Example: `docker-compose up -d --scale payment-svc=3`
    *   Monitor the consumer lag and p95 latency. If scaling helps, it indicates a throughput issue that the development team needs to investigate further.

---

### Scenario 3: Schema Incompatibility Error

**Symptom:** Messages are landing in the DLQ with a `JsonParseException` or a similar deserialization error. Logs for a consumer service show `Could not deserialize ...` errors.

**Triage & Resolution:**

1.  **Identify the Bad Schema:**
    *   This typically happens after a deployment that introduced a breaking change in an event payload.
    *   Confirm with the producing team which service was deployed recently and what changes were made to the event structure.

2.  **Isolate and Revert:**
    *   **Do not retry the messages from the DLQ.** They will only fail again.
    *   The first priority is to **revert the change in the producing service** that introduced the incompatible schema. This could be a code revert or a feature flag toggle.
    *   Once the producer is fixed and deploying, new messages should be processed correctly.

3.  **Handle Failed Messages (Post-Fix):**
    *   This requires manual intervention or a dedicated script.
    *   **Option A (Replay with Transformation):** If possible, create a script that reads from the DLQ, transforms the old message payload to the new schema, and re-publishes it to the original topic.
    *   **Option B (Time-Travel Replay):** If the business logic allows, and the volume of failed data is acceptable to lose/re-process, you could use a "Replay" feature (as described in the spec's point #6) to reset the consumer group's offset to a time before the bad deployment, effectively ignoring the poison pill messages.
    *   **Option C (Manual Correction):** For critical, low-volume data, export the failed messages from the DLQ UI, manually correct the data in the database or via an admin API, and mark the DLQ messages as "purged".
