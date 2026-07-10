# ArchiveOS Review Event Contract

Archive-Market sends profitability review events to ArchiveOS through `market_outbox_event` with target `ARCHIVE_OS`.

## Event Types

- `ORDER_REQUIRES_REVIEW`
- `LOW_MARGIN_ORDER_DETECTED`
- `HIGH_RISK_ORDER_DETECTED`

## Payload Envelope

```json
{
  "eventId": "EVT-...",
  "idempotencyKey": "MARKET:ORDER_REQUIRES_REVIEW:{orderId}",
  "source": "Archive-Market",
  "eventType": "ORDER_REQUIRES_REVIEW",
  "schemaVersion": 1,
  "occurredAt": "2026-07-10T00:00:00Z",
  "simulationRunId": "SIM-...",
  "settlementCycleId": "SETTLEMENT-2026-07-10",
  "correlationId": "CORR-...",
  "causationId": "{orderId}",
  "hopCount": 1,
  "maxHop": 5,
  "payload": {
    "orderId": "ORD-...",
    "customerType": "HIGH_RISK_CUSTOMER",
    "orderAmount": 1200000,
    "expectedRevenue": 1200000,
    "expectedCost": 1130000,
    "expectedProfit": 70000,
    "marginRate": 5.8,
    "riskScore": 0.82,
    "recommendation": "REVIEW_REQUIRED",
    "reason": "Low margin and high risk customer"
  }
}
```

## Safety

- ArchiveOS must treat `idempotencyKey` as unique.
- Events with `hopCount > maxHop` must not be forwarded.
- Review events must not trigger another Market profitability evaluation loop.
