Accounts

Overview
- Accounts represent cash or product-level balances (savings, FD, mutual funds, loans).
- Fields include: `name`, `institution`, `accountNo`, `balance`, `balanceUpdatedDate`, `assetClass`, `accountType`, `retirementAsset`.

Frontend
- Manage accounts under the `ACCOUNTS` tab.
- Add/Edit forms map directly to the backend model.

Endpoints
- GET /api/finance/accounts — list accounts for authenticated user
- POST /api/finance/accounts — create or update account (JSON body)
- DELETE /api/finance/accounts/{id} — delete account

Notes
- `retirementAsset` controls inclusion in retirement projections.
- Balances are used in dashboard net-worth calculations and alerts for insufficient funds.
