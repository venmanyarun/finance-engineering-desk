Export / Import

Overview
- The app supports exporting the entire ledger to a portable JSON and importing it back. This preserves accounts, incomes, obligations, transactions, and portfolios.

Frontend
- Use the `DATA_MANAGEMENT` tab to export JSON or upload a JSON backup.

Backend
- `DataExportImportService` handles export/import and resolves nested references during import (destinationAccount, linkedAccount, transaction account refs).

Notes
- When importing, the service attempts to remap references to existing accounts by name and will create new entities if necessary. Manual validation of the imported JSON is recommended for complex datasets.
