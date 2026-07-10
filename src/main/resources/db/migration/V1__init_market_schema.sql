create table market_customer (
    id bigserial primary key,
    customer_id varchar(80) not null unique,
    customer_type varchar(40) not null,
    risk_level integer not null,
    synthetic_name varchar(160) not null,
    created_at timestamptz not null,
    updated_at timestamptz not null
);

create table market_product (
    id bigserial primary key,
    product_id varchar(80) not null unique,
    product_type varchar(60) not null,
    product_name varchar(160) not null,
    base_price numeric(19,2) not null,
    base_cost numeric(19,2) not null,
    margin_rate numeric(8,4) not null,
    enabled boolean not null,
    created_at timestamptz not null,
    updated_at timestamptz not null
);

create table market_order (
    id bigserial primary key,
    order_id varchar(80) not null unique,
    customer_id varchar(80) not null,
    customer_type varchar(40) not null,
    order_status varchar(40) not null,
    total_order_amount numeric(19,2) not null,
    discount_amount numeric(19,2) not null,
    payment_amount numeric(19,2) not null,
    currency varchar(8) not null,
    risk_score integer not null,
    requires_approval boolean not null,
    created_at timestamptz not null,
    updated_at timestamptz not null
);

create table market_order_item (
    id bigserial primary key,
    order_pk bigint not null references market_order(id),
    order_id varchar(80) not null,
    product_id varchar(80) not null,
    product_type varchar(60) not null,
    quantity integer not null,
    unit_price numeric(19,2) not null,
    unit_cost numeric(19,2) not null,
    line_amount numeric(19,2) not null,
    created_at timestamptz not null
);

create table market_payment (
    id bigserial primary key,
    payment_id varchar(80) not null unique,
    order_id varchar(80) not null,
    payment_status varchar(40) not null,
    amount numeric(19,2) not null,
    payment_method varchar(80) not null,
    captured_at timestamptz,
    refunded_at timestamptz,
    created_at timestamptz not null,
    updated_at timestamptz not null
);

create table market_return (
    id bigserial primary key,
    return_id varchar(80) not null unique,
    order_id varchar(80) not null,
    return_reason varchar(255) not null,
    return_amount numeric(19,2) not null,
    status varchar(40) not null,
    created_at timestamptz not null,
    updated_at timestamptz not null
);

create table market_claim (
    id bigserial primary key,
    claim_id varchar(80) not null unique,
    order_id varchar(80) not null,
    claim_type varchar(80) not null,
    claim_amount numeric(19,2) not null,
    status varchar(40) not null,
    created_at timestamptz not null,
    updated_at timestamptz not null
);

create table market_revenue_event (
    id bigserial primary key,
    event_id varchar(80) not null unique,
    idempotency_key varchar(255) not null unique,
    simulation_run_id varchar(80),
    settlement_cycle_id varchar(80),
    order_id varchar(80),
    revenue_type varchar(80) not null,
    revenue_amount numeric(19,2) not null,
    currency varchar(8) not null,
    reason varchar(255) not null,
    created_at timestamptz not null
);

create table market_cost_event (
    id bigserial primary key,
    event_id varchar(80) not null unique,
    idempotency_key varchar(255) not null unique,
    simulation_run_id varchar(80),
    settlement_cycle_id varchar(80),
    order_id varchar(80),
    cost_type varchar(80) not null,
    cost_amount numeric(19,2) not null,
    currency varchar(8) not null,
    reason varchar(255) not null,
    created_at timestamptz not null
);

create table market_outbox_event (
    id bigserial primary key,
    event_id varchar(80) not null unique,
    idempotency_key varchar(255) not null unique,
    target_service varchar(40) not null,
    event_type varchar(80) not null,
    aggregate_type varchar(80) not null,
    aggregate_id varchar(80) not null,
    payload jsonb not null,
    status varchar(40) not null,
    retry_count integer not null default 0,
    last_error text,
    next_retry_at timestamptz,
    created_at timestamptz not null,
    published_at timestamptz,
    updated_at timestamptz not null
);

create table market_event_inbox (
    id bigserial primary key,
    event_id varchar(80) not null unique,
    idempotency_key varchar(255) not null unique,
    source_service varchar(80) not null,
    event_type varchar(80) not null,
    payload jsonb not null,
    status varchar(40) not null,
    received_at timestamptz not null,
    processed_at timestamptz,
    failure_reason text
);

create table market_daily_close (
    id bigserial primary key,
    close_id varchar(80) not null unique,
    close_date date not null,
    total_revenue numeric(19,2) not null,
    total_cost numeric(19,2) not null,
    total_profit numeric(19,2) not null,
    order_count bigint not null,
    return_count bigint not null,
    claim_count bigint not null,
    status varchar(40) not null,
    created_at timestamptz not null,
    completed_at timestamptz
);

create table market_profit_snapshot (
    id bigserial primary key,
    snapshot_id varchar(80) not null unique,
    simulation_run_id varchar(80),
    settlement_cycle_id varchar(80),
    snapshot_date date not null,
    revenue_amount numeric(19,2) not null,
    cost_amount numeric(19,2) not null,
    profit_amount numeric(19,2) not null,
    cash_balance numeric(19,2) not null,
    burn_rate numeric(19,2) not null,
    bankruptcy_risk varchar(40) not null,
    created_at timestamptz not null
);

create table audit_log (
    id bigserial primary key,
    trace_id varchar(120),
    actor varchar(120) not null,
    action varchar(80) not null,
    target_type varchar(80) not null,
    target_id varchar(120) not null,
    before_status varchar(80),
    after_status varchar(80),
    detail text,
    created_at timestamptz not null
);
