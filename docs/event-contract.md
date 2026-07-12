# Event Contract

Every external event uses an envelope:

```json
{
  "eventId": "EVT-...",
  "idempotencyKey": "MARKET:EVENT_TYPE:aggregateId",
  "source": "Archive-Market",
  "sourceService": "Archive-Market",
  "targetService": "NEXUS",
  "eventType": "PRODUCTION_REQUESTED",
  "orderId": "ORD-...",
  "entityId": "ORD-...",
  "schemaVersion": 1,
  "occurredAt": "2026-07-10T00:00:00Z",
  "simulationRunId": "SIM-...",
  "settlementCycleId": "SETTLEMENT-2026-07-10",
  "correlationId": "CORR-...",
  "causationId": "REV-...",
  "hopCount": 1,
  "maxHop": 5,
  "payload": {
    "eventId": "EVT-...",
    "correlationId": "CORR-...",
    "causationId": "REV-...",
    "orderId": "ORD-...",
    "entityId": "ORD-...",
    "simulationRunId": "SIM-...",
    "workdayId": null,
    "settlementCycleId": null
  }
}
```

Root correlation rule:

- Market generates `root_correlation_id` exactly once when a new synthetic order enters through demand/order creation.
- All derived order events reuse the stored root correlation. A downstream publisher must never generate a replacement.
- `causationId` is the immediately preceding event ID. The root event is the only event allowed to have no causation ID.
- An existing outbox idempotency key cannot be rebound to another correlation ID.

Safety rules:

- reject or ignore events when `hopCount > maxHop`
- enforce unique `eventId`
- enforce unique `idempotencyKey`
- do not generate fee events from fee events
- guard order/payment status before revenue/refund generation
