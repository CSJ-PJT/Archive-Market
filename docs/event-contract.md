# Event Contract

Every external event uses an envelope:

```json
{
  "eventId": "EVT-...",
  "idempotencyKey": "MARKET:EVENT_TYPE:aggregateId",
  "source": "Archive-Market",
  "eventType": "PRODUCTION_REQUESTED",
  "schemaVersion": 1,
  "occurredAt": "2026-07-10T00:00:00Z",
  "simulationRunId": "SIM-...",
  "settlementCycleId": "SETTLEMENT-2026-07-10",
  "correlationId": "CORR-...",
  "causationId": "ORD-...",
  "hopCount": 1,
  "maxHop": 5,
  "payload": {}
}
```

Safety rules:

- reject or ignore events when `hopCount > maxHop`
- enforce unique `eventId`
- enforce unique `idempotencyKey`
- do not generate fee events from fee events
- guard order/payment status before revenue/refund generation
