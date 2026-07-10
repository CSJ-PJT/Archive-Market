# Runtime Event Contract

ArchiveOS Live Flow가 Archive-Market의 runtime event를 read-only로 수집하기 위한 계약입니다.

## 원칙

- 모든 데이터는 Synthetic Runtime Data입니다.
- 실제 개인정보, 주소, 카드번호, 계좌번호, 결제 토큰, secret, webhook, private key를 포함하지 않습니다.
- Frontend에서 fake random animation 데이터를 만들지 않고, ArchiveOS는 이 API 응답만 기준으로 Flow를 그립니다.
- `eventId`, `sourceService`, `eventType`, `status`, `correlationId`, `entityId`를 일관되게 제공합니다.
- ArchiveOS가 꺼져 있어도 Archive-Market은 정상 동작합니다.

## API

| Method | Path | 설명 |
| --- | --- | --- |
| `GET` | `/api/runtime-events/recent?limit=100` | Outbox/Inbox 기반 최신 runtime event |
| `GET` | `/api/runtime-events/correlation/{correlationId}` | 동일 correlation 흐름 조회 |
| `GET` | `/api/runtime-events/entity/{entityId}` | 동일 주문/엔티티 흐름 조회 |

## Response Field

```json
{
  "eventId": "EVT-20260710-ABC",
  "sourceService": "Archive-Market",
  "domain": "market",
  "eventType": "PRODUCTION_REQUESTED",
  "entityType": "order",
  "entityId": "ORD-20260710-001",
  "correlationId": "CORR-20260710-001",
  "causationId": "ORD-20260710-001",
  "status": "waiting",
  "severity": "info",
  "displayLabel": "Archive-Market outbound PRODUCTION_REQUESTED for ORD-20260710-001",
  "occurredAt": "2026-07-10T00:00:00Z",
  "metadata": {
    "direction": "outbound",
    "targetService": "NEXUS",
    "idempotencyKey": "MARKET:PRODUCTION_REQUESTED:ORD-20260710-001",
    "aggregateType": "MARKET_ORDER",
    "retryCount": 0
  }
}
```

## Status Mapping

| Source | Raw status | Runtime status |
| --- | --- | --- |
| Outbox | `PENDING` | `waiting` |
| Outbox | `PUBLISHED`, `DRY_RUN` | `completed` |
| Outbox | `RETRY` | `delayed` |
| Outbox | `FAILED` | `failed` |
| Outbox | `SKIPPED` | `unavailable` |
| Outbox | `ORDER_REQUIRES_REVIEW`, `LOW_MARGIN_ORDER_DETECTED`, `HIGH_RISK_ORDER_DETECTED` | `approval_required` |
| Inbox | `RECEIVED` | `waiting` |
| Inbox | `PROCESSED`, `DUPLICATE` | `completed` |
| Inbox | `REJECTED` | `rejected` |
| Inbox | `FAILED` | `failed` |

## Severity Mapping

| Runtime condition | Severity |
| --- | --- |
| 정상 완료 또는 dry-run 완료 | `normal` |
| 대기 중 이벤트 | `info` |
| retry, skipped, rejected | `warning` |
| failed | `critical` |

## Metadata 허용 범위

허용:

- synthetic event ID
- synthetic order/entity ID
- idempotency key
- target/source service
- aggregate type
- retry count
- processed timestamp

금지:

- 실제 이름
- 전화번호
- 주소
- 카드번호
- 계좌번호
- 실제 결제 토큰
- secret/token/password/webhook/private key
