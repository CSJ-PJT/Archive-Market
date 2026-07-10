# Operations Runbook

Start:

```powershell
docker compose up --build -d
```

Check:

```powershell
curl.exe http://localhost:8094/actuator/health
curl.exe http://localhost:8094/api/runtime/status
curl.exe http://localhost:8094/api/outbox/summary
curl.exe http://localhost:8094/api/operations/summary
curl.exe http://localhost:8094/api/market-economy/summary
curl.exe http://localhost:8094/api/market-workforce/summary
curl.exe http://localhost:8094/api/market-cashflow/summary
curl.exe http://localhost:8094/api/market-productivity/summary
```

Dry-run publish:

```powershell
curl.exe -X POST http://localhost:8094/api/outbox/publish
```

Troubleshooting:

- if DB connection fails, check `archive-market-postgres` health
- if outbox remains `PENDING`, check integration and scheduler flags
- if inbound events are rejected, inspect `hopCount`, `maxHop`, `eventId`, and `idempotencyKey`
- if summary endpoints return 500 with `market_workforce_allocation_workforce_role_key`, verify V7 migration removed the old `workforce_role` unique constraint
- `GET /api/operations/summary` and `GET /api/market-economy/summary` must be read-only and must not seed workforce rows

Repeated summary smoke:

```powershell
for /L %i in (1,1,5) do curl.exe -s -o NUL -w "%{http_code}\n" http://localhost:8094/api/operations/summary
for /L %i in (1,1,5) do curl.exe -s -o NUL -w "%{http_code}\n" http://localhost:8094/api/market-economy/summary
```

After simulation:

```powershell
curl.exe -X POST "http://localhost:8094/api/simulations/orders?count=100"
curl.exe http://localhost:8094/api/operations/summary
curl.exe http://localhost:8094/api/market-economy/summary
```

ArchiveOS should isolate Market as `DEGRADED` only when these read-only summary APIs fail. A Market summary failure must not block Nexus, Logistics, or Ledger collection.

## Autonomous Runtime Work Loop

Archive-Market can run a bounded synthetic runtime loop in local/demo environments so ArchiveOS Live Flow continues to receive fresh runtime events while the service is UP.

Configuration:

```yaml
archive:
  runtime:
    autorun:
      enabled: true
      scheduler-enabled: true
    tick-interval: 30s
    max-events-per-tick: 10
    max-backlog-per-tick: 50
```

Runtime status:

```powershell
curl.exe http://localhost:8094/api/runtime/status
```

Safety rules:

- no real customer, payment, financial, or employee data
- one scheduler lock per JVM
- duplicate tick bucket does not create additional work
- tick work is bounded by `max-events-per-tick`
- order creation stops when backlog exceeds `max-backlog-per-tick`
- external writes still follow integration enabled / dry-run behavior
- summary GET APIs remain read-only
