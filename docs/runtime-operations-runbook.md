# Runtime Operations Runbook

Auto-run creates synthetic demand/order and evaluates profitability. Only ACCEPT orders are confirmed, captured, and sent to Nexus/Ledger. REVIEW_REQUIRED and REJECT_RECOMMENDED orders stay pending without automatic cancellation or payment.

## Runtime 확인

```powershell
curl.exe http://localhost:8094/api/runtime/status
curl.exe "http://localhost:8094/api/runtime-events/recent?limit=20"
curl.exe http://localhost:8094/api/operations/summary
```

`runtimeActive=true`, `schedulerStatus=RUNNING`, `pipelineStatus=LIVE`, `lastEventAt` 및 `latestCursor`가 갱신되는지 확인한다.

## Auto-run 안전장치

- `archive.runtime.autorun.enabled`
- `archive.runtime.tick-interval`
- `archive.runtime.max-events-per-tick`
- `archive.runtime.max-backlog-per-tick`
- scheduler lock 및 같은 tick 중복 guard
- outbox idempotency key, hopCount/maxHop, retry 상한

GET summary 및 runtime event API는 데이터를 생성하지 않는다. 데이터가 준비되지 않은 workforce 계열 API는 `available=false`, `status=NO_DATA`로 응답한다.

## ArchiveOS 장애 격리

ArchiveOS push가 실패해도 Market은 Outbox에 이벤트와 오류 상태를 보존하고 자체 주문 처리를 계속한다. ArchiveOS는 Market의 read API가 응답하지 않을 때에만 해당 서비스를 `DEGRADED`로 표시하고, 다른 서비스의 수집을 계속해야 한다.
