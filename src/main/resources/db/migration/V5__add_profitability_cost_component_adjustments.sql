create table profitability_cost_component_adjustment (
    id bigserial primary key,
    adjustment_id varchar(80) not null unique,
    order_id varchar(80) not null,
    source_service varchar(80) not null,
    source_event_id varchar(80) not null unique,
    idempotency_key varchar(255) not null unique,
    component_type varchar(60) not null,
    amount numeric(19,2) not null,
    currency varchar(8) not null,
    payload jsonb not null,
    applied_at timestamptz not null,
    created_at timestamptz not null
);

create index idx_profitability_cost_adjustment_order on profitability_cost_component_adjustment(order_id);
create index idx_profitability_cost_adjustment_component on profitability_cost_component_adjustment(component_type);
create index idx_profitability_cost_adjustment_source on profitability_cost_component_adjustment(source_service, source_event_id);
