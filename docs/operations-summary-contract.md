# Operations Summary Contract

ArchiveOS Operational Twin이 Archive-Market의 운영 상태를 read-only로 수집하기 위한 `/api/operations/summary` 계약입니다.

## API

```http
GET /api/operations/summary
```

## 핵심 필드

| Field | 설명 |
| --- | --- |
| `serviceName` | `Archive-Market` |
| `serviceRole` | `Synthetic Commerce Backend` |
| `status` | 서비스 상태. 정상 기본값은 `HEALTHY` |
| `latestEventAt` | Outbox/Inbox 기준 최신 runtime event 발생 시각 |
| `liveFlowAvailable` | Live Flow read-only API 제공 여부 |
| `degradedReason` | degraded 상태 사유. 정상 기본값은 `NONE` |
| `outbox.pending` | publish 대기 outbox 수 |
| `outbox.published` | publish 완료 outbox 수 |
| `outbox.failed` | 실패 outbox 수 |
| `outbox.retry` | retry outbox 수 |
| `economy.revenue` | synthetic revenue alias |
| `economy.cost` | synthetic cost alias |
| `economy.profit` | synthetic profit |
| `orders.total` | 전체 synthetic 주문 수 |
| `orders.confirmed` | 확정 주문 수 |
| `payments.captured` | captured synthetic payment 수 |
| `profitability.reviewRequired` | ArchiveOS 검토가 필요한 주문 수 |
| `cashflow.availableCash` | Market 운영 기준 synthetic available cash |
| `cashflow.pendingSettlementAmount` | Ledger 최종 정산 전 pending settlement 금액 |
| `workforce.totalHeadcount` | synthetic workforce 총 headcount |
| `workforce.effectiveCapacity` | 생산성 반영 주문 처리 capacity |
| `workforce.usedCapacity` | 현재 주문 기준 사용 capacity |
| `workforce.backlog` | capacity 초과 주문 수 |

## 예시

```json
{
  "serviceName": "Archive-Market",
  "serviceRole": "Synthetic Commerce Backend",
  "service": "Archive-Market",
  "status": "HEALTHY",
  "latestEventAt": "2026-07-10T00:00:00Z",
  "liveFlowAvailable": true,
  "degradedReason": "NONE",
  "economy": {
    "totalRevenue": 120000000,
    "totalCost": 76000000,
    "revenue": 120000000,
    "cost": 76000000,
    "profit": 44000000,
    "cashBalance": 94000000,
    "burnRate": 0,
    "bankruptcyRisk": "LOW"
  },
  "outbox": {
    "pending": 10,
    "published": 900,
    "failed": 0,
    "retry": 2
  },
  "workforce": {
    "totalHeadcount": 12,
    "effectiveCapacity": 520,
    "usedCapacity": 100,
    "backlog": 0
  }
}
```

## 호환성

- 기존 `service`, `orders`, `economy.totalRevenue`, `cashflow`, `profitability`, `productivity`, `integration` 필드는 유지합니다.
- 새 필드는 ArchiveOS 공통 수집 계약을 위한 additive 변경입니다.
- ArchiveOS 장애나 미실행 상태는 이 API 응답 생성에 영향을 주지 않습니다.
