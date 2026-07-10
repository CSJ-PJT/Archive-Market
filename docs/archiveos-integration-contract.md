# ArchiveOS Integration Contract

ArchiveOS reads:

- `GET /api/operations/summary`
- `GET /api/market-economy/summary`
- `GET /api/outbox/summary`
- `GET /actuator/health`
- `GET /actuator/metrics`

Summary fields include:

- service status
- order counts
- revenue, cost, profit
- cash balance
- burn rate
- bankruptcy risk
- return rate
- claim rate
- high risk order count
- outbox and inbox status
- last daily close

Archive-Market can also enqueue `MARKET_ECONOMY_SUMMARY_UPDATED` to `ARCHIVE_OS` after daily close. Integration is disabled by default and publishes as dry-run unless `MARKET_INTEGRATION_ENABLED=true`.
