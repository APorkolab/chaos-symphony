# Payment Service

This service is responsible for handling payment processing for an order.

## Responsibilities

-   **Process Payments:** Listens for `payment.requested` events from the orchestrator.
-   **Simulate Payment Logic:** Simulates a payment attempt (in this demo, it succeeds or fails randomly).
-   **Publish Results:** Publishes a `payment.result` event indicating whether the charge was successful or not.
-   **Idempotency:** Uses an idempotency store to ensure that the same payment request is not processed more than once.

## Canary Mode

This service can be run in "canary" mode by activating the `canary` Spring profile. When in canary mode, it listens to the `payment.requested.canary` topic and uses a different consumer group (`payment-requested-canary`). This allows for testing a new version of the service on a small percentage of live traffic.
