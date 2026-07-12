alter table market_order
    add column if not exists root_correlation_id varchar(120),
    add column if not exists last_event_id varchar(80);

alter table order_profitability_assessment
    add column if not exists causation_event_id varchar(80);

create unique index if not exists uk_market_order_root_correlation
    on market_order (root_correlation_id)
    where root_correlation_id is not null;

create index if not exists ix_market_order_last_event_id
    on market_order (last_event_id)
    where last_event_id is not null;
