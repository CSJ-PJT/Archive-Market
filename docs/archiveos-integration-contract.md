# ArchiveOS Integration Contract

ArchiveOS reads:

- `GET /api/operations/summary`
- `GET /api/market-economy/summary`
- `GET /api/market-workforce/summary`
- `GET /api/market-cashflow/summary`
- `GET /api/market-productivity/summary`
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
- workforce headcount, capacity, used capacity, backlog
- cashflow available cash and pending settlement amount
- productivity score and delay risk
- last daily close

Archive-Market can also enqueue `MARKET_ECONOMY_SUMMARY_UPDATED` to `ARCHIVE_OS` after daily close. Integration is disabled by default and publishes as dry-run unless `MARKET_INTEGRATION_ENABLED=true`.

## Read-only Summary Rule

ArchiveOS collection must treat Market summary APIs as read-only telemetry.

The following endpoints do not insert or seed workforce allocation rows:

- `GET /api/operations/summary`
- `GET /api/market-economy/summary`
- `GET /api/market-workforce/summary`
- `GET /api/market-cashflow/summary`
- `GET /api/market-productivity/summary`

If workforce data is empty, Market returns empty roles and zero capacity/backlog values instead of creating defaults during GET.

## Workforce Idempotency

Market workforce allocation uniqueness is:

```text
workdayId + workforceRole
```

The old `workforce_role` single-column unique constraint is invalid for Operational Twin because the same role can be allocated again on a different workday. V7 migration removes that constraint and creates `uk_market_workforce_workday_role`.

## DEGRADED Isolation

If ArchiveOS cannot read Market summary APIs, ArchiveOS should mark only Archive-Market as `DEGRADED` and continue collecting Archive-Nexus, Archive-Logistics, and Archive-Ledger.

Suggested Market smoke:

```powershell
curl.exe http://localhost:8094/api/operations/summary
curl.exe http://localhost:8094/api/market-economy/summary
curl.exe http://localhost:8094/api/market-workforce/summary
curl.exe http://localhost:8094/api/market-cashflow/summary
curl.exe http://localhost:8094/api/market-productivity/summary
```
