# Ledger Integration Contract

Outbound target: `LEDGER`

Events:

- `SALES_REVENUE_CONFIRMED`
- `PAYMENT_CAPTURED`
- `REFUND_REQUESTED`
- `CLAIM_COMPENSATION_CONFIRMED`
- `MARKET_SERVICE_FEE_PAID`
- `PAYMENT_PROCESSING_FEE_PAID`

Example payload:

```json
{
  "eventId": "EVT-...",
  "idempotencyKey": "MARKET:SALES_REVENUE_CONFIRMED:ORD-...",
  "source": "Archive-Market",
  "eventType": "SALES_REVENUE_CONFIRMED",
  "schemaVersion": 1,
  "hopCount": 1,
  "maxHop": 5,
  "payload": {
    "orderId": "ORD-...",
    "customerId": "CUST-...",
    "revenueType": "PRODUCT_SALES_REVENUE_RECOGNIZED",
    "amount": 1200000,
    "currency": "KRW",
    "reason": "Synthetic product sales revenue from Archive-Market"
  }
}
```

Default local endpoint: `http://host.docker.internal:18080/api/events/external`.
