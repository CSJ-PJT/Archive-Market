# Customer Risk Profile

`customer_risk_profile` stores synthetic risk estimates for profitability evaluation.

## Fields

- `customerId`
- `customerType`
- `riskLevel`
- `returnProbability`
- `claimProbability`
- `discountSensitivity`
- `expectedLtv`
- `orderCount`
- `returnCount`
- `claimCount`

## API

- `GET /api/customers/{customerId}/risk-profile`
- `POST /api/customers/{customerId}/risk-profile/recalculate`

## Synthetic Defaults

- `HIGH_RISK_CUSTOMER`: higher return and claim probability.
- `RETURN_PRONE_CUSTOMER`: highest return probability.
- `DISCOUNT_SEEKER`: highest discount sensitivity.
- `VIP_CUSTOMER` and `B2B_CUSTOMER`: lower return and claim probability with higher expected LTV.

The profile is recalculated from synthetic order, return, and claim history when an order is evaluated.
