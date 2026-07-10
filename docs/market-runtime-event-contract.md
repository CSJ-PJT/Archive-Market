# Market Runtime Event Contract

Archive-Market은 ArchiveOS Live Flow의 시작점입니다. 이 문서는 주문, 결제, 수익성, 자금흐름, 인력 처리 결과를 runtime event projection으로 노출하는 계약입니다.

## API

| Method | Path | 설명 |
| --- | --- | --- |
| `GET` | `/api/runtime-events/recent?limit=100` | 최신 Market runtime event projection |
| `GET` | `/api/runtime-events/correlation/{correlationId}` | 동일 business flow 추적 |
| `GET` | `/api/runtime-events/entity/{entityId}` | 주문, 결제, workday 등 entity 기준 추적 |

## Projected Event Types

| eventType | 원천 | entityType | 설명 |
| --- | --- | --- | --- |
| `CUSTOMER_DEMAND_CREATED` | `market_revenue_event` | `demand` | synthetic 고객 수요 생성 |
| `MARKET_ORDER_PLACED` | `market_outbox_event` | `order` | Nexus로 전달할 Market 주문 생성 이벤트 |
| `PAYMENT_CAPTURED` | `market_outbox_event`, `market_revenue_event` | `payment` | synthetic 결제 성공 및 Ledger 전달 이벤트 |
| `ORDER_PROFITABILITY_EVALUATED` | `order_profitability_assessment` | `order` | 주문 수익성 평가 완료 |
| `ORDER_REQUIRES_REVIEW` | `market_outbox_event` | `order` | ArchiveOS 승인 검토 필요 |
| `LOW_MARGIN_ORDER_DETECTED` | `market_outbox_event` | `order` | 저마진 주문 감지 |
| `HIGH_RISK_ORDER_DETECTED` | `market_outbox_event` | `order` | 고위험 주문 감지 |
| `REFUND_REQUESTED` | `market_outbox_event` | `order` | Ledger 환불 요청 |
| `CLAIM_COMPENSATION_CONFIRMED` | `market_outbox_event` | `claim` | Ledger 클레임 보상 정산 요청 |
| `WORKDAY_COMPLETED` | `market_workday_snapshot` | `workday` | synthetic workday 처리 결과 확정 |
| `CAPACITY_SHORTAGE_DETECTED` | `market_workday_snapshot` | `workday` | 처리 capacity 부족 감지 |
| `BACKLOG_INCREASED` | `market_workday_snapshot` | `workday` | backlog 증가 감지 |

## Correlation Policy

주문 기반 outbound event는 동일 주문 흐름을 유지하기 위해 다음 값을 사용합니다.

```text
correlationId = CORR-{orderId}
causationId = {orderId}
```

적용 대상:

- Nexus 대상 주문/생산/출하/취소/반품/클레임 이벤트
- Ledger 대상 매출/결제/환불/클레임 정산 이벤트
- ArchiveOS 대상 review, low margin, high risk 이벤트

## Synthetic Data Policy

- `orderId`, `paymentId`, `customerId`, `assessmentId`, `snapshotId`는 synthetic ID만 사용합니다.
- 실제 고객명처럼 보이는 값은 runtime event metadata에 노출하지 않습니다.
- 고객 표시가 필요한 경우 `customerType` 또는 `syntheticName`처럼 synthetic임이 명확한 필드만 사용합니다.
- Frontend는 Live Flow용 fake random animation 데이터를 만들지 않고 이 runtime API를 읽어야 합니다.
- Dashboard의 sample payload는 문서/시연용 `DEMO/SIMULATION` 데이터이며 runtime API projection과 분리됩니다.

## Example

```json
{
  "eventId": "RTE-ASM-20260710-001",
  "sourceService": "Archive-Market",
  "domain": "market",
  "eventType": "ORDER_PROFITABILITY_EVALUATED",
  "entityType": "order",
  "entityId": "ORD-20260710-001",
  "correlationId": "CORR-ORD-20260710-001",
  "causationId": "ORD-20260710-001",
  "status": "approval_required",
  "severity": "warning",
  "displayLabel": "Archive-Market evaluated profitability for ORD-20260710-001",
  "occurredAt": "2026-07-10T00:00:00Z",
  "metadata": {
    "direction": "internal",
    "assessmentId": "ASM-20260710-001",
    "customerId": "CUS-20260710-001",
    "customerType": "HIGH_RISK_CUSTOMER",
    "recommendation": "REVIEW_REQUIRED",
    "marginRate": 5.8,
    "riskScore": 0.82,
    "expectedProfit": 70000
  }
}
```
