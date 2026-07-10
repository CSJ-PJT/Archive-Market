# Operations Runbook

Start:

```powershell
docker compose up --build -d
```

Check:

```powershell
curl.exe http://localhost:8094/actuator/health
curl.exe http://localhost:8094/api/operations/summary
curl.exe http://localhost:8094/api/outbox/summary
```

Dry-run publish:

```powershell
curl.exe -X POST http://localhost:8094/api/outbox/publish
```

Troubleshooting:

- if DB connection fails, check `archive-market-postgres` health
- if outbox remains `PENDING`, check integration and scheduler flags
- if inbound events are rejected, inspect `hopCount`, `maxHop`, `eventId`, and `idempotencyKey`
