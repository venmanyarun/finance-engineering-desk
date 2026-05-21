# Financial Engineering Desk Node (v2.0)

An enterprise-grade financial command terminal for high-precision asset tracking, lifecycle obligation modeling, and long-term cash-flow forecasting. Designed for individual operators who require professional-grade visibility into their personal balance sheets and protection portfolios.

---

## 🛠️ Advanced Engineering Architecture

- **Primary Ledger Framework:** Java 17, Spring Boot 3.2.4, Hibernate JPA.
- **Isolated Multi-Database Node:** Uses a dual-datasource layout to decouple **Authentication** (`authdb`) from **Financial Data** (`financedb`). Resetting your ledger will never affect your secure access.
- **Transaction Engine:** Built-in support for Manual, Recurring, and Event-based transactions with full ACID compliance.
- **Client Presentation:** React 18, Vite, and **Recharts** for high-fidelity data visualization.

---

## 💎 Premium Features

### 1. Protection & Asset Intelligence
- **Standardized Asset Classes**: Track holdings across `CASH_EQUIVALENTS`, `FIXED_INCOME`, `EQUITIES`, `RETIREMENT`, and `REAL_ESTATE`.
- **Total Death Cover Analysis**: Real-time dashboard calculation of your total "Sum Assured" across all insurance-linked obligations.
- **Nominee Management**: Record and track beneficiaries for every individual account and long-term policy.

### 2. Lifecycle Obligation Matrix
- **Linked-Account Protection**: Link specific bank accounts to each obligation. The console automatically provides ⚠️ **CRITICAL: Insufficient Funds** alerts if an upcoming bill exceeds your bank balance.
- **Lifecycle Modeling**: Define `startDate` and `endDate` for all obligations (Loans, SIPs, Insurances) to see exactly when your future cash flow will "Step Down" or "Step Up".
- **Future Return Integration**: Model survival benefits, annuities, and one-time maturity payouts directly within your outflow commitments.

### 3. High-Precision Forecasting
- **Projection Lens**: Select any date range to see cumulative inflows, outflows, and savings capacity.
- **Visual Growth Trajectory**: Interactive Net Worth charts showing 5-year growth based on real-time compounded monthly surpluses.
- **Annualized Simulation**: Compare raw mathematical annual totals against calendar-aware simulations that factor in mid-year expirations and payouts.

### 4. Zero-Local-Footprint Backup
- **Excel Reporting**: One-click generation of multi-tab `.xlsx` reports (Accounts, Incomes, Obligations, and Transactions).
- **Portable JSON Nodes**: Export your entire state to a portable JSON schema or import existing backups instantly.

---

## 🚀 One-Command Deployment

The project includes a unified deployment script for Windows environments.

### Prerequisites
- **JDK 17** and **Node.js 18+** installed in PATH.

### Installation
Run the following command from the project root:
```powershell
./deploy.bat
```
This script will build the backend, install frontend dependencies, and launch both the API and the Dashboard simultaneously.

---

## 🔐 Operator Access
- **Default Dashboard URL**: [http://localhost:3000](http://localhost:3000)
- **Initial Credentials**:
  - **Username**: `admin`
  - **Password**: `admin`

*Note: Your credentials are stored in an isolated `authdb` and will persist even if you delete the main `financedb` ledger file.*

## 📁 Portfolios & Market Data (new)

This release adds a lightweight portfolio/holdings feature and a snapshot-based market-price import flow.

- Frontend: `PORTFOLIOS` tab in the dashboard (create portfolio, add holdings, import price CSVs).
- Backend endpoints (authenticated):
  - `GET /api/finance/portfolios` — list user portfolios
  - `POST /api/finance/portfolios` — create portfolio
  - `DELETE /api/finance/portfolios/{id}` — delete portfolio and its holdings
  - `GET /api/finance/portfolios/{id}/holdings` — list holdings
  - `POST /api/finance/portfolios/{id}/holdings` — add holding
  - `DELETE /api/finance/holdings/{id}` — delete a single holding
  - `POST /api/finance/market-prices/import-file` — multipart CSV upload (columns: `symbol,exchange,price[,timestamp]`)

Sample CSV (header optional):
```
symbol,exchange,price,timestamp
RELIANCE,NSE,2435.50,2026-05-20T10:00:00
HDFCBANK,NSE,1823.00,2026-05-20T10:00:00
```

Curl examples:
```
# create portfolio
curl -X POST -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" -d '{"name":"My Portfolio","currency":"INR"}' http://localhost:8080/api/finance/portfolios

# upload price CSV
curl -X POST -H "Authorization: Bearer $TOKEN" -F file=@prices.csv http://localhost:8080/api/finance/market-prices/import-file
```

Troubleshooting
- If the frontend build or script fails on Windows PowerShell when using `cd` in automated scripts, run commands manually in two terminals (backend and frontend):
```
# backend
cd E:\Repos\finance-engineering-desk
mvn spring-boot:run

# frontend (separate shell)
cd E:\Repos\finance-engineering-desk\frontend
npm run dev
```

If you hit permission or authentication errors while creating/deleting portfolios, check the browser network request for the failing endpoint and paste the response body and backend logs when asking for help.

## 📚 Full Documentation

Detailed documentation for core features is available in the `docs/` folder:

- [Accounts](docs/ACCOUNTS.md)
- [Obligations](docs/OBLIGATIONS.md)
- [Transactions](docs/TRANSACTIONS.md)
- [Export / Import](docs/EXPORT-IMPORT.md)
- [Retirement Planning](docs/RETIREMENT.md)
- [Projections](docs/PROJECTIONS.md)
- [Authentication](docs/AUTH.md)
- [Portfolios](docs/PORTFOLIOS.md)
- [Market Data](docs/MARKET-DATA.md)

Open the corresponding file in `docs/` for endpoint examples, UI notes, and troubleshooting tips.
