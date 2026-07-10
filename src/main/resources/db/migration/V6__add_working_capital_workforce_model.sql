create table market_workforce_allocation (
    id bigserial primary key,
    workforce_role varchar(60) not null unique,
    headcount integer not null,
    capacity_per_day integer not null,
    wage_per_day numeric(19,2) not null,
    productivity_score numeric(8,4) not null,
    enabled boolean not null,
    created_at timestamptz not null,
    updated_at timestamptz not null
);

create table market_workday_snapshot (
    id bigserial primary key,
    snapshot_id varchar(80) not null unique,
    work_date date not null,
    order_count bigint not null,
    processing_capacity bigint not null,
    backlog_count bigint not null,
    available_cash numeric(19,2) not null,
    working_capital numeric(19,2) not null,
    payroll_cost numeric(19,2) not null,
    net_profit numeric(19,2) not null,
    productivity_score numeric(8,4) not null,
    recommendation varchar(255) not null,
    created_at timestamptz not null
);

create index idx_market_workday_snapshot_date on market_workday_snapshot(work_date);
create index idx_market_workforce_enabled on market_workforce_allocation(enabled);
