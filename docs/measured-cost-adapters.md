# Measured Cost Component Adapters

Archive-Market accepts measured synthetic cost events from Archive-Nexus, Archive-Logistics, and Archive-Ledger through the existing external event inbox.

ArchiveOS is intentionally excluded from this adapter scope.

## Entry Point

- `POST /api/events/external`
- `POST /api/events/external/bulk`

The inbox still applies duplicate and hop safety before the adapter runs.

## Supported Sources

- `Archive-Nexus`: production/manufacturing cost
- `Archive-Logistics`: logistics/shipment/delivery cost
- `Archive-Ledger`: ledger settlement fee or payment processing fee

## Payload Fields

Common required payload field:

- `orderId`

Nexus production cost candidates:

- `actualProductionCost`
- `productionCost`
- `manufacturingCost`
- `totalCost`

Logistics cost candidates:

- `actualLogisticsCost`
- `logisticsCost`
- `shipmentCost`
- `deliveryCost`
- `totalCost`

Ledger settlement fee candidates:

- `ledgerSettlementFee`
- `settlementFee`
- `ledgerFee`
- `feeAmount`

Payment processing fee candidates:

- `paymentProcessingFee`
- `processingFee`
- `feeAmount`

## Example

```json
{
  "eventId": "NEXUS-COST-001",
  "idempotencyKey": "NEXUS:PRODUCTION_COST:ORD-001",
  "source": "Archive-Nexus",
  "eventType": "PRODUCTION_COMPLETED",
  "schemaVersion": 1,
  "occurredAt": "2026-07-10T00:00:00Z",
  "simulationRunId": "SIM-001",
  "settlementCycleId": "SETTLEMENT-2026-07-10",
  "correlationId": "CORR-001",
  "causationId": "ORD-001",
  "hopCount": 1,
  "maxHop": 5,
  "payload": {
    "orderId": "ORD-001",
    "actualProductionCost": 820000,
    "currency": "KRW"
  }
}
```

## Persistence

Every applied measured component is recorded in `profitability_cost_component_adjustment`.

The matching `order_profitability_assessment` component is updated and expected total cost, expected profit, margin rate, and recommendation are recalculated.

## Safety

- Unknown orders are ignored by the adapter after inbox persistence.
- Events without a recognized cost component are ignored by the adapter.
- Duplicate `eventId` or `idempotencyKey` is not applied twice.
- Adapter updates do not publish ArchiveOS review events. ArchiveOS integration will be handled separately.
