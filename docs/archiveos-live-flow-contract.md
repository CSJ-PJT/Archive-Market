# ArchiveOS Live Flow Contract

ArchiveOS Live Flow와 Operational Twin이 Archive-Market의 runtime 상태를 수집하기 위한 통합 계약입니다.

## 목적

ArchiveOS는 Archive-Market의 runtime event, outbox, inbox, workforce, capacity, productivity, economy 상태를 read-only로 수집합니다. Archive-Market은 ArchiveOS가 꺼져 있거나 느려도 주문, 결제, outbox, simulation 흐름을 계속 처리합니다.

## Read-only API

| Method | Path | 용도 |
| --- | --- | --- |
| `GET` | `/api/runtime-events/recent?limit=100` | Live Flow 최신 이벤트 |
| `GET` | `/api/runtime-events/correlation/{correlationId}` | correlation 단위 추적 |
| `GET` | `/api/runtime-events/entity/{entityId}` | 주문/entity 단위 추적 |
| `GET` | `/api/workforce/summary` | 공통 workforce summary |
| `GET` | `/api/productivity/summary` | 공통 productivity summary |
| `GET` | `/api/capacity/summary` | 공통 capacity summary |
| `GET` | `/api/operations/summary` | Operational Twin 종합 상태 |
| `GET` | `/api/market-economy/summary` | Market economy 상태 |
| `GET` | `/api/outbox/summary` | Outbox 상태 |
| `GET` | `/api/events/inbox` | 수신 이벤트 목록 |

기존 Market 전용 API도 유지합니다.

- `GET /api/market-workforce/summary`
- `GET /api/market-productivity/summary`
- `GET /api/market-cashflow/summary`

## Runtime Event Mapping

| 원천 | Runtime event 의미 |
| --- | --- |
| `market_outbox_event` | Market이 Nexus, Ledger, ArchiveOS로 보내려는 outbound event |
| `market_event_inbox` | Nexus, Logistics, Ledger, ArchiveOS가 Market으로 보낸 inbound event |

Outbox event는 `targetService`, `aggregateType`, `aggregateId`, `idempotencyKey`, `retryCount`를 metadata로 제공합니다. Inbox event는 `sourceService`, `idempotencyKey`, `processedAt`을 metadata로 제공합니다.

## Workforce / Capacity / Productivity

ArchiveOS가 공통 경로로 읽는 값입니다.

```json
{
  "totalHeadcount": 12,
  "effectiveCapacity": 520,
  "usedCapacity": 100,
  "backlog": 0,
  "capacityUtilization": 19.23
}
```

Market의 workforce는 실제 직원 정보가 아닌 synthetic workforce입니다.

- `ORDER_OPERATOR`
- `PRICING_ANALYST`
- `CUSTOMER_SUPPORT`
- `CLAIM_HANDLER`
- `MARKET_MANAGER`

## Economy / Cashflow

Archive-Market은 Ledger의 최종 원장 기준과 별개로 운영 현금흐름을 제공합니다.

- `availableCash`
- `expectedReceivable`
- `pendingSettlementAmount`
- `payrollCost`
- `productionRequestCost`
- `logisticsRequestCost`
- `ledgerFee`
- `netProfit`
- `workingCapital`

## ArchiveOS 수집 규칙

- 모든 API는 read-only로 호출합니다.
- `correlationId`를 기준으로 서비스 간 이벤트 흐름을 연결합니다.
- `entityId`는 주문, 결제, 클레임 등 synthetic entity 추적에 사용합니다.
- metadata는 화면 표시용 보조 정보이며 실제 민감 정보를 포함하지 않습니다.
- `liveFlowAvailable=false` 또는 응답 실패 시 ArchiveOS는 해당 서비스를 degraded로 표시하고 다른 서비스 수집을 계속합니다.

## 금지 데이터

- 실제 이름
- 전화번호
- 주소
- 카드번호
- 계좌번호
- 실제 결제 토큰
- 실제 직원/급여 개인정보
- secret/token/password/webhook/private key
