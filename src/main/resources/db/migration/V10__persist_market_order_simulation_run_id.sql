alter table market_order
    add column if not exists simulation_run_id varchar(80);

create index if not exists ix_market_order_simulation_run_id
    on market_order (simulation_run_id)
    where simulation_run_id is not null;
