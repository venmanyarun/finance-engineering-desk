Obligations (Recurring commitments)

Overview
- Obligations model recurring outflows (bills, SIPs, EMIs, insurance premiums).
- Important fields: `instrumentName`, `amount`, `frequency` (MONTHLY/QUARTERLY/ANNUAL), `nextDueDate`, `linkedAccount`, `category`.

Frontend
- Manage obligations in the `OBLIGATIONS` tab. Use the Add form to define a recurring commitment.
- For mutual fund SIPs, include `sipSymbol` or set `referenceNo` to the fund identifier; marking an `INVESTMENT_SIP` obligation as paid creates/updates a `Holding`.

Endpoints
- GET /api/finance/obligations
- POST /api/finance/obligations
- DELETE /api/finance/obligations/{id}

Notes
- `recordEvent` flows advance `nextDueDate` when obligations are marked paid.
- Linked accounts are debited when obligations are processed; ensure sufficient balance to avoid alerts.
