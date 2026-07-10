# Nexus Integration Contract

Outbound target: `NEXUS`

Events:

- `MARKET_ORDER_PLACED`
- `PRODUCTION_REQUESTED`
- `SHIPMENT_REQUESTED`
- `ORDER_CANCELLED`
- `RETURN_REQUESTED`
- `QUALITY_CLAIM_CREATED`

Example payload:

```json
{
  "eventId": "EVT-...",
  "idempotencyKey": "MARKET:PRODUCTION_REQUESTED:ORD-...",
  "source": "Archive-Market",
  "eventType": "PRODUCTION_REQUESTED",
  "schemaVersion": 1,
  "hopCount": 1,
  "maxHop": 5,
  "payload": {
    "orderId": "ORD-...",
    "customerType": "B2B_CUSTOMER",
    "productType": "BATTERY_MODULE",
    "quantity": 10,
    "orderAmount": 1200000,
    "priority": "HIGH",
    "requiresShipment": true
  }
}
```

Default local endpoint: `http://host.docker.internal:8080/api/events/external`.
