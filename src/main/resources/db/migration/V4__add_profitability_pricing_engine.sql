create table pricing_policy (
    id bigserial primary key,
    policy_code varchar(120) not null unique,
    policy_type varchar(40) not null,
    target_customer_type varchar(40),
    target_product_type varchar(60),
    fixed_amount numeric(19,2),
    rate numeric(10,6),
    threshold_amount numeric(19,2),
    enabled boolean not null,
    created_at timestamptz not null,
    updated_at timestamptz not null
);

create table order_profitability_assessment (
    id bigserial primary key,
    assessment_id varchar(80) not null unique,
    order_id varchar(80) not null,
    customer_id varchar(80) not null,
    customer_type varchar(40) not null,
    product_type varchar(60) not null,
    order_amount numeric(19,2) not null,
    expected_revenue numeric(19,2) not null,
    estimated_production_cost numeric(19,2) not null,
    estimated_logistics_cost numeric(19,2) not null,
    estimated_ledger_fee numeric(19,2) not null,
    payment_processing_fee numeric(19,2) not null,
    discount_cost numeric(19,2) not null,
    expected_return_cost numeric(19,2) not null,
    expected_claim_cost numeric(19,2) not null,
    customer_acquisition_cost numeric(19,2) not null,
    market_operation_cost numeric(19,2) not null,
    expected_total_cost numeric(19,2) not null,
    expected_profit numeric(19,2) not null,
    margin_rate numeric(8,4) not null,
    risk_score numeric(8,4) not null,
    return_probability numeric(8,4) not null,
    claim_probability numeric(8,4) not null,
    recommendation varchar(40) not null,
    approval_required boolean not null,
    reason varchar(255) not null,
    created_at timestamptz not null
);

create table customer_risk_profile (
    id bigserial primary key,
    customer_id varchar(80) not null unique,
    customer_type varchar(40) not null,
    risk_level integer not null,
    return_probability numeric(8,4) not null,
    claim_probability numeric(8,4) not null,
    discount_sensitivity numeric(8,4) not null,
    expected_ltv numeric(19,2) not null,
    order_count bigint not null,
    return_count bigint not null,
    claim_count bigint not null,
    updated_at timestamptz not null
);

create table price_recommendation (
    id bigserial primary key,
    recommendation_id varchar(80) not null unique,
    order_id varchar(80),
    product_type varchar(60) not null,
    base_price numeric(19,2) not null,
    recommended_price numeric(19,2) not null,
    min_acceptable_price numeric(19,2) not null,
    target_margin_rate numeric(8,4) not null,
    reason varchar(255) not null,
    created_at timestamptz not null
);

create unique index ux_order_profitability_assessment_order on order_profitability_assessment(order_id);
create index idx_order_profitability_recommendation on order_profitability_assessment(recommendation);
create index idx_order_profitability_margin on order_profitability_assessment(margin_rate);
create index idx_order_profitability_risk on order_profitability_assessment(risk_score);
create index idx_customer_risk_profile_type on customer_risk_profile(customer_type);
create index idx_price_recommendation_product on price_recommendation(product_type);
