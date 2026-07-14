alter table market_outbox_event
    add column if not exists publish_approved boolean not null default false;

create index if not exists ix_market_outbox_publish_approved_status_created
    on market_outbox_event (publish_approved, status, created_at);
