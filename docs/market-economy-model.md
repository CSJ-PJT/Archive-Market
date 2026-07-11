# Market Economy Model

Archive-Market separates gross transaction volume from Market-recognized revenue.

GMV:

- Sum of synthetic order total amount.
- GMV is not treated as Market profit.
- Legacy gross sales events can still exist for integration compatibility, but summary separates them as `grossSalesEvents`.

Recognized Revenue:

- platform fee revenue
- payment processing fee revenue
- optional service fee
- B2B/service fee
- express order fee
- service contract revenue
- claim recovery revenue

Expense:

- production purchase cost payable to Archive-Nexus
- logistics fulfillment fee payable to Archive-Logistics
- settlement agency fee payable to Archive-Ledger
- control tower / orchestration fee payable to ArchiveOS
- workforce payroll cost
- promotion / coupon / discount cost
- refund reserve
- claim reserve
- risk reserve allocation
- market operation cost
- optional inventory / holding / emergency surcharge

Formula:

```text
GMV = sum(order.totalOrderAmount)
Recognized Revenue = fee-based Market revenue
Total Expense = ecosystem payables + reserves + operating costs
Operating Profit = Recognized Revenue - Total Expense
Cash Balance = 50,000,000 KRW synthetic baseline
  + Recognized Revenue
  - Total Expense
  - Pending Settlement Amount
Burn Rate = abs(Operating Profit) when profit is negative
```

`GET /api/market-economy/summary` exposes:

- `gmv`
- `grossSalesEvents`
- `recognizedRevenue`
- `totalExpense`
- `operatingProfit`
- `cashBalance`
- `reserveBalance`
- `outstandingPayables`
- `pendingSettlementAmount`
- `cashDeltaReason`
- `topRevenueDrivers`
- `topExpenseDrivers`

Notes:

- GMV is total synthetic order scale, not Market net revenue.
- `recognizedRevenue` is primarily platform, payment, and service fee revenue.
- `operatingProfit` subtracts production, logistics, settlement, control tower, payroll, promotion, and reserve costs from recognized revenue.
- With accumulated synthetic data, `operatingProfit` can be negative. That is acceptable when ecosystem payables and reserves exceed fee-based recognized revenue.

Bankruptcy risk:

- `HIGH`: negative cash or burn rate over 10,000,000
- `MEDIUM`: negative profit or cash under 15,000,000
- `LOW`: otherwise
