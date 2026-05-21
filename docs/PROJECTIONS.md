Projections & Forecasting

Overview
- The ProjectionService computes future inflows/outflows and net worth trajectories based on current accounts, incomes, and obligations.
- Useful for cash flow planning and scenario analysis.

Frontend
- `PROJECTIONS` tab and various dashboard charts render projection output.

Endpoints
- GET /api/finance/forecast?years=NUM — returns time series projection (monthly) for specified horizon.
- GET /api/finance/retirement-projection — retirement-specific projection (see RETIREMENT.md)

Notes
- Projections assume deterministic recurring flows; enhancements may include stochastic returns or user-defined rate assumptions.
