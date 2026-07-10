# Order Profitability & Pricing Engine

Archive-Market evaluates each synthetic order before confirmation/payment flow decisions are consumed by the ecosystem. The engine is policy based in this first implementation and does not call real PG, card, logistics, ledger, address, or customer data providers.

## Formula

Expected Revenue =

- order amount
- express order fee estimate
- service contract revenue estimate
- premium handling fee estimate

Expected Cost =

- estimated production cost
- estimated logistics cost
- ledger settlement fee
- payment processing fee
- discount cost
- expected return cost
- expected claim cost
- customer acquisition cost
- market operation cost

Expected Profit = Expected Revenue - Expected Cost

Expected Margin Rate = Expected Profit / Expected Revenue * 100

## Recommendation

- `ACCEPT`: margin rate is at least 15% and risk score is below 0.6.
- `REVIEW_REQUIRED`: margin is between 5% and 15%, risk is at least 0.75, order amount is at least 3,000,000 KRW, customer type is `HIGH_RISK_CUSTOMER`, return probability is at least 0.3, or claim probability is at least 0.2.
- `REJECT_RECOMMENDED`: margin is below 5% or expected profit is negative.

Reject is advisory only. The engine records a recommendation and does not cancel the order unless a future configuration or ArchiveOS decision applies that action.

## ArchiveOS Integration

Review events are written to Market Outbox with target `ARCHIVE_OS`:

- `ORDER_REQUIRES_REVIEW`
- `LOW_MARGIN_ORDER_DETECTED`
- `HIGH_RISK_ORDER_DETECTED`

Every event includes `simulationRunId`, `settlementCycleId`, `correlationId`, `causationId`, `hopCount`, `maxHop`, and `idempotencyKey`.

## Extension Points

The current estimates can later be replaced by measured inputs from Archive-Nexus production cost, Archive-Logistics freight cost, and Archive-Ledger settlement fee data. The assessment table keeps each cost component separate to support that transition.
