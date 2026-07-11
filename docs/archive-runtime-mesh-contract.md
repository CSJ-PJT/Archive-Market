# Archive Runtime Mesh V1

Archive-Market은 ArchiveOS Console V3가 읽는 Synthetic Runtime Data 제공자다. 모든 조회 API는 읽기 전용이며, 조회 중 주문 생성, seed, outbox publish, settlement 실행을 하지 않는다.

## 공통 Read API

| API | 목적 |
| --- | --- |
| `GET /api/runtime/status` | runtime loop 상태와 최신 cursor |
| `GET /api/runtime-events/recent?limit=100` | 최신 runtime event 목록 |
| `GET /api/runtime-events/recent?after={cursor}&limit=100` | cursor 이후 event를 오래된 순서로 조회 |
| `GET /api/runtime-events/correlation/{correlationId}` | correlation 기반 흐름 조회 |
| `GET /api/runtime-events/entity/{entityId}` | synthetic entity 기반 흐름 조회 |
| `GET /api/operations/summary` | Market 운영 종합 상태 |
| `GET /api/workforce/summary` | workforce 상태 |
| `GET /api/productivity/summary` | productivity 상태 |
| `GET /api/capacity/summary` | capacity/backlog 상태 |

`after` cursor는 `occurredAt + eventId`의 URL-safe opaque 값이다. ArchiveOS는 마지막으로 처리한 event의 `cursor`를 저장하고 다음 polling에 그대로 전달한다. cursor 이후 조회는 순서를 보장하기 위해 오래된 event부터 반환한다.

## Runtime Event Envelope

```json
{
  "eventId": "EVT-SYNTHETIC",
  "idempotencyKey": "MARKET:PAYMENT_CAPTURED:ORD-SYNTHETIC",
  "sourceService": "Archive-Market",
  "targetService": "LEDGER",
  "domain": "market",
  "eventType": "PAYMENT_CAPTURED",
  "entityType": "order",
  "entityId": "ORD-SYNTHETIC",
  "correlationId": "CORR-ORD-SYNTHETIC",
  "causationId": "ORD-SYNTHETIC",
  "simulationRunId": "SIM-SYNTHETIC",
  "settlementCycleId": "SETTLEMENT-SYNTHETIC",
  "workdayId": null,
  "status": "COMPLETED",
  "severity": "NORMAL",
  "occurredAt": "2026-07-11T00:00:00Z",
  "hopCount": 1,
  "maxHop": 5,
  "cursor": "opaque-cursor",
  "metadata": {"direction": "outbound", "retryCount": 0}
}
```

외부 Outbox/Inbox event는 원본 envelope의 `simulationRunId`, `settlementCycleId`, `correlationId`, `causationId`, `hopCount`, `maxHop`을 보존한다. 내부 projection은 outbox 대상이 없으므로 `targetService`가 null일 수 있다. metadata에는 synthetic ID 및 운영 수치만 넣는다.

## 데이터 부재

workforce, productivity, capacity snapshot이 아직 없으면 임의의 0값을 만들지 않는다. 다음 형태로 응답한다.

```json
{"available": false, "status": "NO_DATA", "reason": "No workforce allocation is available"}
```

## Market Balance Projection

GET /api/market-economy/summary returns explicit Market balance metrics:

- gmv, grossSalesEvents, recognizedRevenue
- totalExpense, operatingProfit, operatingMargin
- cashBalance, reserveBalance, outstandingPayables, pendingSettlementAmount
- workforceCost, productionPurchaseCost, logisticsFulfillmentCost
- settlementAgencyFee, controlTowerFee
- backlogCount, capacityUtilization, negativeProfitStreak

GMV is order scale, not Market revenue or profit. recognizedRevenue is fee-based, and operatingMargin is calculated as operatingProfit / recognizedRevenue * 100. Market exposes synthetic results without forcing a target margin.

All four financial metrics use calculationScope=LIFETIME: recognizedRevenue uses all recognized revenue events, totalExpense uses all cost events, and operatingProfit/operatingMargin are derived from those same lifetime totals. periodStart, periodEnd, calculatedAt, and dataAvailable are included in the summary.
