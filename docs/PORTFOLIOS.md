Portfolios & Holdings

Overview
- Portfolios are user-scoped containers for investment holdings (equities, mutual funds, etc.).
- Holdings belong to a portfolio and track `symbol`, `exchange`, `quantity`, `avgPrice`, and `acquiredDate`.

Key endpoints
- GET /api/finance/portfolios — list portfolios for current user
- POST /api/finance/portfolios — create a portfolio (body: JSON { name, currency })
- DELETE /api/finance/portfolios/{id} — delete a portfolio and its holdings
- GET /api/finance/portfolios/{id}/holdings — list holdings
- POST /api/finance/portfolios/{id}/holdings — add holding (body: JSON { symbol, exchange, quantity, avgPrice, acquiredDate })
- DELETE /api/finance/holdings/{id} — delete a single holding

Frontend
- Open the `PORTFOLIOS` tab in the dashboard.
- Create a portfolio using the form (name + currency).
- Use the per-portfolio Add holding form to record holdings.
- Use the "Delete Portfolio" button in the portfolio header to remove the portfolio and all its holdings (confirmation prompt shown).
- Use the per-holding "Drop" button to remove a single holding.

Notes
- Operations require an authenticated session. If you see 401/403 responses, login via the Dashboard login form first.
- After create/delete actions, the UI refreshes the dashboard state automatically.
