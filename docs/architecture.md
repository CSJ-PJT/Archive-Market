# Architecture

Archive-Market is the synthetic commerce entrypoint for Archive Platform Ecosystem.

It models external demand, sales order creation, synthetic payment capture, revenue recognition, discount costs, payment fees, returns, claims, daily close, and profit snapshots.

Main boundaries:

- `customer`: synthetic customer profiles and risk level
- `product`: product catalog and pricing policy
- `order`: order lifecycle and Nexus request trigger
- `payment`: synthetic capture/refund lifecycle
- `revenue`: revenue, cost, daily close, profit snapshot, ArchiveOS summary
- `claim`: returns and quality claims
- `outbox`: durable outbound event contract
- `inbox`: idempotent external event receiver
- `simulation`: demand/order/day synthetic scenario runner
- `operations`: service health and business summary

Archive-Market does not modify ArchiveOS, Archive-Nexus, Archive-Logistics, or Archive-Ledger repositories.
