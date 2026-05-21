Market Data (snapshot-based)

Overview
- Market prices are stored as immutable snapshots (`MarketPrice` entries). The UI uses the latest snapshot per symbol+exchange to value holdings.
- This system is intentionally simple: manual or CSV-based snapshot imports are supported for the MVP.

CSV format
- Accepts CSV with columns: `symbol,exchange,price[,timestamp]`.
- Timestamp if present must be ISO local `yyyy-MM-ddTHH:mm:ss`, otherwise server uses current time.

Endpoints
- POST /api/finance/market-prices/import — body: raw CSV text
- POST /api/finance/market-prices/import-file — multipart file upload (`file` field)
- GET /api/finance/market-prices?symbol=SYMBOL&exchange=EXCHANGE — returns latest snapshot for that symbol+exchange

Example CSV
```
symbol,exchange,price,timestamp
RELIANCE,NSE,2435.50,2026-05-20T10:00:00
HDFCBANK,NSE,1823.00,2026-05-20T10:00:00
```

Recommended next steps
- Implement periodic snapshot ingestion from a market-data provider (AlphaVantage, Yahoo, etc.) in `MarketDataServiceImpl` as a scheduled job.
- Add per-portfolio currency conversion (FX) if you store holdings in different currencies.
