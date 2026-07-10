# Simulation Scenario

Synthetic endpoints:

- `POST /api/simulations/demand?count=100`
- `POST /api/simulations/orders?count=100`
- `POST /api/simulations/day/run?date=YYYY-MM-DD`

Rules:

- `count` must be between 1 and 10000
- response returns summary, not full event payloads
- generated data is synthetic only
- order simulation creates order, confirmation, payment capture, revenue/cost events, and outbox events
- day run adds returns, claims, and daily close
