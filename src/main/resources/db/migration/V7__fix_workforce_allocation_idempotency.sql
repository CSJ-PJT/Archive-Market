alter table market_workforce_allocation
    add column if not exists workday_id varchar(80);

update market_workforce_allocation
set workday_id = 'DEFAULT'
where workday_id is null;

alter table market_workforce_allocation
    alter column workday_id set not null;

alter table market_workforce_allocation
    drop constraint if exists market_workforce_allocation_workforce_role_key;

alter table market_workforce_allocation
    drop constraint if exists uk_market_workforce_workday_role;

alter table market_workforce_allocation
    add constraint uk_market_workforce_workday_role unique (workday_id, workforce_role);

create index if not exists idx_market_workforce_workday
    on market_workforce_allocation(workday_id);
