Transactions

Overview
- Transactions record ledger events: manual entries, income receipts, and obligation payments.
- Each transaction links source/destination accounts and can be created via UI actions or the `recordEvent` endpoint.

Frontend
- `Mark Paid` buttons in the dashboard trigger `recordEvent` which creates a `Transaction`, updates accounts, and advances obligation dates.
- Manual transactions are available under `TRANSACTIONS` with a form for ad-hoc entries.

Endpoints
- GET /api/finance/transactions — list recent transactions
- POST /api/finance/transactions/manual — create manual transaction
- POST /api/finance/transactions/record-event — process obligation/income events (body: { id, type })

Notes
- Transaction creation is atomic and updates balances used across the dashboard.
