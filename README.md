# Archive-Market

Archive-Market은 Archive Platform의 외부 고객 수요, 주문, 결제, 매출, 반품, 클레임 이벤트를 synthetic data로 생성하는 Spring Boot 기반 Commerce Backend입니다.

이 저장소는 포트폴리오와 비즈니스 시뮬레이션을 위한 백엔드입니다. 실제 개인정보, 실제 주소, 실제 카드번호, 실제 결제정보, 실제 금융 데이터, 실제 PG/카드/배송사 API를 사용하지 않습니다.

## Ecosystem Role

- Archive-Market: 외부 수요, 주문, 결제, 매출, 반품, 클레임 이벤트 생성
- Archive-Nexus: Market 주문 기반 생산, 재고, 출하 이벤트 생성
- Archive-Logistics: Nexus 출하 기반 배송, 운송비, 지연 비용 계산
- Archive-Ledger: Market 매출/환불/클레임, Nexus 비용, Logistics 비용 정산
- ArchiveOS: 손익, cash, risk, approval, settlement 상태 관제

## Stack

Java 21, Spring Boot 3.x, Gradle, Spring Web, Validation, Spring Data JPA, PostgreSQL, Flyway, Spring Batch, Actuator, Micrometer, JUnit 5, AssertJ, Testcontainers, Docker, Docker Compose.

## Run

```powershell
docker compose up --build -d
curl.exe http://localhost:8094/actuator/health
curl.exe http://localhost:8094/api/operations/summary
```

Local profile defaults:

- app: `8094`
- PostgreSQL: `15435:5432`
- integration enabled: `false`
- outbox publish: dry-run

## API

- `GET /api/customers`
- `GET /api/customers/{customerId}/risk-profile`
- `POST /api/customers/{customerId}/risk-profile/recalculate`
- `GET /api/products`
- `POST /api/products/seed`
- `GET /api/pricing/policies`
- `POST /api/pricing/policies/seed`
- `POST /api/pricing/recommend`
- `POST /api/orders`
- `POST /api/orders/simulate?count=100`
- `GET /api/orders`
- `GET /api/orders/{orderId}`
- `POST /api/orders/{orderId}/profitability/evaluate`
- `GET /api/orders/{orderId}/profitability`
- `POST /api/orders/{orderId}/confirm`
- `POST /api/orders/{orderId}/cancel`
- `POST /api/payments/capture?orderId={orderId}`
- `POST /api/payments/refund?orderId={orderId}`
- `GET /api/payments`
- `POST /api/returns?orderId={orderId}`
- `POST /api/claims?orderId={orderId}`
- `GET /api/market-economy/summary`
- `GET /api/market-profitability/summary`
- `GET /api/market-profitability/assessments`
- `POST /api/market-economy/daily-close?date=YYYY-MM-DD`
- `GET /api/outbox/summary`
- `POST /api/outbox/publish`
- `POST /api/events/external`
- `POST /api/simulations/orders?count=100`
- `POST /api/simulations/profitability?count=100`
- `POST /api/simulations/day/run?date=YYYY-MM-DD`
- `GET /api/operations/summary`

## Order Profitability & Pricing Engine

Archive-Market evaluates synthetic order profitability with estimated revenue, production cost, logistics cost, Ledger settlement fee, payment fee, discount cost, expected return cost, expected claim cost, customer acquisition cost, and market operation cost.

Recommendations:

- `ACCEPT`
- `REVIEW_REQUIRED`
- `REJECT_RECOMMENDED`

Low-margin or high-risk orders are written to the outbox for ArchiveOS review as `ORDER_REQUIRES_REVIEW`, `LOW_MARGIN_ORDER_DETECTED`, and `HIGH_RISK_ORDER_DETECTED`. The recommendation is advisory by default and does not cancel orders unless `archive.market.profitability.block-low-margin-orders` is enabled in a future decision flow.

## Event Safety

All outbound external events include:

- `simulationRunId`
- `settlementCycleId`
- `correlationId`
- `causationId`
- `hopCount`
- `maxHop`
- `idempotencyKey`

Inbound events are rejected when `hopCount > maxHop`. Duplicate `eventId` or `idempotencyKey` is not processed again.

## Smoke Test

```powershell
curl.exe -X POST http://localhost:8094/api/products/seed
curl.exe -X POST "http://localhost:8094/api/simulations/orders?count=100"
curl.exe http://localhost:8094/api/market-economy/summary
curl.exe http://localhost:8094/api/outbox/summary
curl.exe -X POST http://localhost:8094/api/outbox/publish
```

When `MARKET_INTEGRATION_ENABLED=false`, outbox publish marks pending events as `DRY_RUN` instead of calling Nexus, Ledger, or ArchiveOS.
