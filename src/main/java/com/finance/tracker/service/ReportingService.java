package com.finance.tracker.service;

import com.finance.tracker.domain.*;
import com.finance.tracker.repository.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
public class ReportingService {

    private static final Logger log = LoggerFactory.getLogger(ReportingService.class);

    @Autowired private FinancialAccountRepository accountRepository;
    @Autowired private IncomeSourceRepository incomeRepository;
    @Autowired private RecurringObligationRepository obligationRepository;
    @Autowired private TransactionRepository transactionRepository;

    public byte[] generateFinancialExcelReport(User user) throws IOException {
        log.info("generateFinancialExcelReport called for user: {}", user.getId());
        Long userId = user.getId();
        try (Workbook workbook = new XSSFWorkbook()) {
            // 1. Accounts Sheet
            Sheet accountSheet = workbook.createSheet("Accounts & Assets");
            List<FinancialAccount> accounts = accountRepository.findByUserId(userId);
            String[] accHeaders = {"Name", "Institution", "Asset Class", "Account Type", "Balance", "Updated Date"};
            fillSheet(accountSheet, accHeaders, accounts, (row, acc) -> {
                row.createCell(0).setCellValue(acc.getName());
                row.createCell(1).setCellValue(acc.getInstitution());
                row.createCell(2).setCellValue(acc.getAssetClass().name());
                row.createCell(3).setCellValue(acc.getAccountType().name());
                row.createCell(4).setCellValue(acc.getBalance().doubleValue());
                row.createCell(5).setCellValue(acc.getBalanceUpdatedDate().toString());
            });

            // 2. Income Sheet
            Sheet incomeSheet = workbook.createSheet("Income Sources");
            List<IncomeSource> incomes = incomeRepository.findByUserId(userId);
            String[] incHeaders = {"Source", "Amount", "Frequency", "Start Date", "Next Date", "Status"};
            fillSheet(incomeSheet, incHeaders, incomes, (row, inc) -> {
                row.createCell(0).setCellValue(inc.getName());
                row.createCell(1).setCellValue(inc.getAmount().doubleValue());
                row.createCell(2).setCellValue(inc.getFrequency().name());
                row.createCell(3).setCellValue(inc.getStartDate().toString());
                row.createCell(4).setCellValue(inc.getNextExpectedDate() != null ? inc.getNextExpectedDate().toString() : "");
                row.createCell(5).setCellValue(inc.isActive() ? "Active" : "Inactive");
            });

            // 3. Obligations Sheet
            Sheet oblSheet = workbook.createSheet("Recurring Obligations");
            List<RecurringObligation> obligations = obligationRepository.findByUserId(userId);
            String[] oblHeaders = {"Description", "Category", "Amount", "Frequency", "Next Due Date", "Linked Account"};
            fillSheet(oblSheet, oblHeaders, obligations, (row, obl) -> {
                row.createCell(0).setCellValue(obl.getInstrumentName());
                row.createCell(1).setCellValue(obl.getCategory().name());
                row.createCell(2).setCellValue(obl.getAmount().doubleValue());
                row.createCell(3).setCellValue(obl.getFrequency().name());
                row.createCell(4).setCellValue(obl.getNextDueDate().toString());
                row.createCell(5).setCellValue(obl.getLinkedAccount() != null ? obl.getLinkedAccount().getName() : "None");
            });

            // 4. Transactions Sheet
            Sheet txSheet = workbook.createSheet("Transaction History");
            List<Transaction> transactions = transactionRepository.findByUserIdOrderByTransactionDateDesc(userId);
            String[] txHeaders = {"Date", "Description", "Type", "Amount", "Account"};
            fillSheet(txSheet, txHeaders, transactions, (row, tx) -> {
                row.createCell(0).setCellValue(tx.getTransactionDate().toString());
                row.createCell(1).setCellValue(tx.getDescription());
                row.createCell(2).setCellValue(tx.getType().name());
                row.createCell(3).setCellValue(tx.getAmount().doubleValue());
                row.createCell(4).setCellValue(tx.getSourceAccount() != null ? tx.getSourceAccount().getName() : 
                                            (tx.getDestinationAccount() != null ? tx.getDestinationAccount().getName() : "N/A"));
            });

            // Auto-size columns for all sheets
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                Sheet sheet = workbook.getSheetAt(i);
                if (sheet.getRow(0) != null) {
                    for (int j = 0; j < sheet.getRow(0).getLastCellNum(); j++) {
                        sheet.autoSizeColumn(j);
                    }
                }
            }

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            workbook.write(bos);
            return bos.toByteArray();
        }
    }

    private <T> void fillSheet(Sheet sheet, String[] headers, List<T> data, RowFiller<T> filler) {
        Row headerRow = sheet.createRow(0);
        CellStyle headerStyle = sheet.getWorkbook().createCellStyle();
        Font font = sheet.getWorkbook().createFont();
        font.setBold(true);
        headerStyle.setFont(font);
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowNum = 1;
        for (T item : data) {
            Row row = sheet.createRow(rowNum++);
            filler.fill(row, item);
        }
    }

    @FunctionalInterface
    private interface RowFiller<T> {
        void fill(Row row, T item);
    }
}