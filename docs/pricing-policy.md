# Pricing Policy

Pricing policy is represented by `pricing_policy` and exposed through:

- `GET /api/pricing/policies`
- `POST /api/pricing/policies/seed`
- `POST /api/pricing/recommend`

## Default Values

- Payment processing fee rate: `2.0%`
- Ledger settlement fee rate: `0.3%`
- Ledger fixed fee: `100 KRW`
- Basic logistics estimate: `50,000 KRW`
- Urgent logistics surcharge estimate: `30,000 KRW`
- Cold chain estimate: `80,000 KRW`

## Customer Acquisition Cost

- `RETAIL_CUSTOMER`: `5,000 KRW`
- `B2B_CUSTOMER`: `20,000 KRW`
- `VIP_CUSTOMER`: `30,000 KRW`
- `HIGH_RISK_CUSTOMER`: `10,000 KRW`
- `DISCOUNT_SEEKER`: `7,000 KRW`
- `RETURN_PRONE_CUSTOMER`: `8,000 KRW`

## Discount Policy

- `DISCOUNT_SEEKER`: `10%`
- `VIP_CUSTOMER`: `5%`
- `B2B_CUSTOMER`: `3%`

Price recommendations calculate a minimum acceptable price from target margin and base cost, then recommend the larger value between the current base price and that minimum acceptable price.
