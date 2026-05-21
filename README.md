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
