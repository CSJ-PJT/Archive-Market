# Market Economy Model

Market Revenue:

- product sales revenue
- B2B contract revenue
- express order fee
- service contract revenue
- claim recovery revenue

Market Cost:

- customer acquisition cost
- discount cost
- payment processing fee
- return cost
- claim compensation cost
- market operation cost
- bad debt cost

Formula:

```text
Market Profit = Market Revenue - Market Cost
Cash Balance = 50,000,000 KRW synthetic baseline + Market Profit
Burn Rate = abs(Market Profit) when profit is negative
```

Bankruptcy risk:

- `HIGH`: negative cash or burn rate over 10,000,000
- `MEDIUM`: negative profit or cash under 15,000,000
- `LOW`: otherwise
