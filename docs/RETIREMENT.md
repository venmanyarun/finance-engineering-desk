Retirement planning and projection

Overview
- Retirement flags on `FinancialAccount` and `RecurringObligation` allow the system to treat certain assets/instruments as retirement-related.
- The retirement projection endpoint provides a time series projection of retirement balances based on configured retirement assets and recurring contributions.

Frontend
- `RETIREMENT` tab shows retirement projection charts and allows configuring projection horizon.

Endpoints
- GET /api/finance/retirement-projection?fromMonth=YYYY-MM-DD&toMonth=YYYY-MM-DD

Notes
- Retirement assets are included in the projection; instruments marked as `retirementInstrument` (e.g., certain obligations) are treated as contributions.
