alter table order_profitability_assessment
    add column if not exists simulation_run_id varchar(80);

create index if not exists ix_order_profitability_assessment_simulation_run_id
    on order_profitability_assessment (simulation_run_id)
    where simulation_run_id is not null;
