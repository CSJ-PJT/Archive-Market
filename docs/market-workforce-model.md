# Market Workforce Model

Archive-Market의 workforce는 실제 직원 데이터가 아닌 Synthetic Workforce입니다.

## 역할

- `ORDER_OPERATOR`
- `PRICING_ANALYST`
- `CUSTOMER_SUPPORT`
- `CLAIM_HANDLER`
- `MARKET_MANAGER`

각 allocation은 다음 값을 가집니다.

- `workdayId`
- `workforceRole`
- `headcount`
- `capacityPerDay`
- `wagePerDay`
- `productivityScore`
- `enabled`

## Idempotency

`market_workforce_allocation`은 `allocation_id` 대신 DB primary key를 내부 식별자로 사용하고, business uniqueness는 다음 복합 제약으로 관리합니다.

```sql
unique (workday_id, workforce_role)
```

규칙:

- 같은 `workdayId + workforceRole`은 하나의 allocation만 허용합니다.
- 같은 role이라도 다른 workday에는 다시 배정할 수 있습니다.
- 중복 배정 요청은 insert가 아니라 기존 row update로 처리합니다.
- 기본 workday는 `DEFAULT`입니다.

## Summary Read-only 원칙

다음 GET API는 DB insert/seed를 수행하지 않습니다.

- `GET /api/operations/summary`
- `GET /api/market-economy/summary`
- `GET /api/market-workforce/summary`
- `GET /api/market-cashflow/summary`
- `GET /api/market-productivity/summary`
- `GET /api/workforce/summary`
- `GET /api/productivity/summary`
- `GET /api/capacity/summary`

데이터가 없으면 0, empty roles, empty/default summary를 반환합니다.

## Seed / Allocate

쓰기 동작은 명시적 command에서만 수행합니다.

- `POST /api/market-workforce/allocate`
- simulation/workday command
- service 내부 명시적 `seedDefaults()` 호출

`seedDefaults()`는 여러 번 호출해도 `workday_id + workforce_role` 기준으로 중복 insert를 만들지 않습니다.

## Troubleshooting

오류:

```text
duplicate key value violates unique constraint "market_workforce_allocation_workforce_role_key"
```

의미:

- 이전 schema가 `workforce_role` 단독 unique 제약을 사용하고 있습니다.
- V7 migration이 적용되어야 합니다.

확인:

```sql
select constraint_name
from information_schema.table_constraints
where table_name = 'market_workforce_allocation';
```

정상 상태:

- `market_workforce_allocation_workforce_role_key` 없음
- `uk_market_workforce_workday_role` 존재
