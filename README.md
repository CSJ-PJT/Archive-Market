<p align="center">
  <img src="src/main/resources/static/assets/archive-logo.png" width="120" alt="Archive 로고">
</p>

# Archive-Market

Archive-Market은 Archive Platform Ecosystem의 외부 수요, 주문, 결제, 매출, 반품, 클레임 이벤트를 생성하고 주문별 수익성 판단까지 수행하는 Spring Boot 기반 Synthetic Commerce Backend입니다.

이 저장소는 실제 상거래 서비스가 아니라 포트폴리오와 비즈니스 시뮬레이션을 위한 합성 데이터 기반 백엔드입니다. 실제 개인정보, 실제 주소, 실제 카드번호, 실제 결제정보, 실제 금융 데이터, 실제 PG/카드/배송사 API를 사용하지 않습니다.

## 역할

- 외부 고객 수요와 주문 생성
- 결제, 매출, 환불, 클레임 이벤트 생성
- Nexus 생산/출하 요청 이벤트 생성
- Ledger 매출/환불/수수료 정산 이벤트 생성
- ArchiveOS가 읽을 수 있는 economy, operations, profitability summary 제공
- 주문별 예상 수익성, 위험도, 할인 가능 여부 평가
- 저마진/고위험 주문을 ArchiveOS 검토 대상으로 outbox 기록
- Nexus/Logistics/Ledger 실측 비용 이벤트를 assessment cost component에 반영

## Ecosystem 연결

- Archive-Market: 수요, 주문, 결제, 매출, 수익성 판단
- Archive-Nexus: Market 주문 기반 생산, 재고, 출하 이벤트
- Archive-Logistics: Nexus 출하 기반 배송, 운송비, 지연 비용
- Archive-Ledger: Market 매출/환불/클레임, Nexus 비용, Logistics 비용 정산
- ArchiveOS: 손익, cash, risk, approval, settlement 상태 관제

## 기술 스택

- Java 21
- Spring Boot 3.x
- Gradle
- Spring Web
- Spring Validation
- Spring Data JPA
- PostgreSQL
- Flyway
- Spring Batch
- Actuator / Micrometer
- JUnit 5 / AssertJ / Testcontainers
- Docker / Docker Compose
- GitHub Actions

## 실행

```powershell
docker compose up --build -d
curl.exe http://localhost:8094/actuator/health
curl.exe http://localhost:8094/api/operations/summary
```

기본 포트:

- 애플리케이션: `8094`
- PostgreSQL: `15435:5432`

기본 profile:

- `local`

## 웹 화면

- 홈페이지: `http://localhost:8094/`
- 운영 대시보드: `http://localhost:8094/dashboard/`

웹 화면과 브라우저 북마크 아이콘은 `src/main/resources/static/assets/archive-logo.png` 로고를 사용합니다.

## 주요 API

### Operations

- `GET /actuator/health`
- `GET /actuator/info`
- `GET /api/operations/summary`

### Customer / Product

- `GET /api/customers`
- `GET /api/customers/{customerId}/risk-profile`
- `POST /api/customers/{customerId}/risk-profile/recalculate`
- `GET /api/products`
- `POST /api/products/seed`

### Order / Payment

- `POST /api/orders`
- `GET /api/orders`
- `GET /api/orders/{orderId}`
- `POST /api/orders/{orderId}/confirm`
- `POST /api/orders/{orderId}/cancel`
- `POST /api/payments/capture?orderId={orderId}`
- `POST /api/payments/refund?orderId={orderId}`
- `GET /api/payments`

### Profitability / Pricing

- `GET /api/pricing/policies`
- `POST /api/pricing/policies/seed`
- `POST /api/pricing/recommend`
- `POST /api/orders/{orderId}/profitability/evaluate`
- `GET /api/orders/{orderId}/profitability`
- `GET /api/orders/{orderId}/profitability/cost-adjustments`
- `GET /api/market-profitability/summary`
- `GET /api/market-profitability/assessments`

### Returns / Claims

- `POST /api/returns?orderId={orderId}`
- `POST /api/claims?orderId={orderId}`
- `GET /api/returns`
- `GET /api/claims`

### Economy / Outbox / Inbox

- `GET /api/market-economy/summary`
- `GET /api/market-economy/revenue-events`
- `GET /api/market-economy/cost-events`
- `GET /api/market-economy/profit-snapshots`
- `POST /api/market-economy/daily-close?date=YYYY-MM-DD`
- `GET /api/outbox/summary`
- `GET /api/outbox/events`
- `POST /api/outbox/publish`
- `POST /api/outbox/retry-failed`
- `POST /api/events/external`
- `POST /api/events/external/bulk`
- `GET /api/events/inbox`

### Simulation

- `POST /api/simulations/demand?count=100`
- `POST /api/simulations/orders?count=100`
- `POST /api/simulations/profitability?count=100`
- `POST /api/simulations/day/run?date=YYYY-MM-DD`

## 주문 수익성 및 가격 정책 엔진

Archive-Market은 주문을 무조건 확정하지 않고, 예상 매출과 비용을 계산해 주문별 권고 결과를 남깁니다.

Expected Revenue:

- 주문 금액
- Express fee 추정
- Service contract revenue 추정
- Premium handling fee 추정

Expected Cost:

- 예상 생산 비용
- 예상 물류 비용
- Ledger 정산 수수료
- 결제 수수료
- 할인 비용
- 기대 반품 비용
- 기대 클레임 비용
- 고객 획득 비용
- Market 운영 비용

권고 결과:

- `ACCEPT`
- `REVIEW_REQUIRED`
- `REJECT_RECOMMENDED`

`REJECT_RECOMMENDED`는 권고값이며 주문을 자동 취소하지 않습니다. 실제 취소/승인은 별도 API 또는 ArchiveOS 의사결정 흐름에서 처리하도록 설계했습니다.

## 실측 비용 어댑터

Nexus, Logistics, Ledger가 보내는 synthetic 실측 비용 이벤트는 기존 external inbox로 수신합니다.

- Archive-Nexus: 생산/제조 비용을 `PRODUCTION_COST`에 반영
- Archive-Logistics: 배송/운송 비용을 `LOGISTICS_COST`에 반영
- Archive-Ledger: 정산 수수료와 결제 처리 수수료를 반영

적용된 실측 비용은 `profitability_cost_component_adjustment`에 기록되고, 기존 `order_profitability_assessment`의 총비용, 예상이익, 마진율, recommendation을 재계산합니다.

## 이벤트 안전장치

모든 외부 이벤트 envelope에는 다음 필드를 포함합니다.

- `simulationRunId`
- `settlementCycleId`
- `correlationId`
- `causationId`
- `hopCount`
- `maxHop`
- `idempotencyKey`

안전 규칙:

- `hopCount > maxHop` 이벤트는 거부
- `eventId` 또는 `idempotencyKey` 중복 이벤트는 재처리하지 않음
- payment/refund/review/fee 이벤트가 다시 무한 평가를 만들지 않도록 상태와 idempotency 기반으로 방어

## Smoke Test

```powershell
curl.exe -X POST http://localhost:8094/api/products/seed
curl.exe -X POST "http://localhost:8094/api/simulations/orders?count=100"
curl.exe http://localhost:8094/api/market-economy/summary
curl.exe http://localhost:8094/api/market-profitability/summary
curl.exe http://localhost:8094/api/outbox/summary
curl.exe -X POST http://localhost:8094/api/outbox/publish
```

`market.integration.enabled=false`이면 outbox publish는 Nexus, Ledger, ArchiveOS를 실제 호출하지 않고 `DRY_RUN`으로 처리합니다.

## 문서

- `docs/architecture.md`
- `docs/event-contract.md`
- `docs/profitability-engine.md`
- `docs/pricing-policy.md`
- `docs/customer-risk-profile.md`
- `docs/measured-cost-adapters.md`
- `docs/archiveos-review-event-contract.md`
- `docs/nexus-integration-contract.md`
- `docs/ledger-integration-contract.md`
- `docs/archiveos-integration-contract.md`
