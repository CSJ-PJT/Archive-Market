# Portfolio Bullets

Archive-Market은 외부 고객 수요·주문·결제·매출 이벤트를 synthetic data로 생성해 Archive-Nexus의 생산/출하 흐름과 Archive-Ledger의 매출/환불/클레임 정산 흐름을 시작시키는 Spring Boot 기반 Commerce Backend입니다.

- Java 21, Spring Boot 3, PostgreSQL, Flyway 기반 commerce backend
- Outbox Pattern과 idempotency key로 event-driven 통합 안정성 설계
- simulationRunId, settlementCycleId, correlationId, causationId, hopCount, maxHop 기반 순환 이벤트 방지
- 수익/비용 이벤트, daily close, profit snapshot, bankruptcy risk 계산
- ArchiveOS Survival Mode가 읽을 operations/economy summary 제공
- Testcontainers 기반 PostgreSQL 통합 테스트 구성
