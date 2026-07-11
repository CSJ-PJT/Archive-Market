# ArchiveOS Realtime Integration

ArchiveOS가 동작하지 않아도 Archive-Market의 주문, 결제, 수익성, runtime loop는 계속 처리된다.

## Pull 방식

기본 수집 방식은 `GET /api/runtime-events/recent?after={cursor}&limit=100` polling이다. Market은 runtime event를 삭제하지 않으며, ArchiveOS는 마지막 성공 cursor를 보관한다. 최초 동기화에는 `GET /api/runtime-events/recent?limit=100`을 사용한다.

## Push 방식

Market outbox target이 `ARCHIVE_OS`이고 `market.integration.enabled=true`인 경우에만 `ARCHIVEOS_PUBLISH_PATH`로 publish한다. 기본 경로는 `/api/live-flow/events/ingest`다.

전송 실패는 주문 transaction을 rollback하지 않는다. event는 outbox에 남고 30초, 60초, 120초, 최대 300초 backoff로 재시도한다. 기본 최대 retry는 5회이며, 초과하면 `SKIPPED`와 실패 사유를 남긴다. ArchiveOS ingest는 같은 `eventId`와 `idempotencyKey`를 재수신해도 안전해야 한다.

