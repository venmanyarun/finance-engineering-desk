package com.finance.tracker.controller;

import com.finance.tracker.domain.*;
import com.finance.tracker.repository.*;
import com.finance.tracker.service.DataExportImportService;
import com.finance.tracker.service.ProjectionService;
import com.finance.tracker.service.ReportingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.finance.tracker.domain.Transaction.TransactionType.INSURANCE_PREMIUM;

@RestController
@RequestMapping("/api/finance")
@CrossOrigin(origins = "http://localhost:3000")
public class FinanceController {

    @Autowired private FinancialAccountRepository accountRepository;
    @Autowired private RecurringObligationRepository obligationRepository;
    @Autowired private IncomeSourceRepository incomeRepository;
    @Autowired private TransactionRepository transactionRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private DataExportImportService dataService;
    @Autowired private ProjectionService projectionService;
    @Autowired private ReportingService reportingService;

    private Long getCurrentUserId() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username).orElseThrow().getId();
    }

    @GetMapping("/dashboard-summary")
    public Map<String, Object> getDashboardSummary() {
        Long userId = getCurrentUserId();
        Map<String, Object> metrics = new HashMap<>();
        List<FinancialAccount> accounts = accountRepository.findByUserId(userId);
        List<IncomeSource> incomes = incomeRepository.findByUserId(userId);
        List<RecurringObligation> obligations = obligationRepository.findByUserId(userId);

        BigDecimal totalAssets = BigDecimal.ZERO;
        BigDecimal totalLiabilities = BigDecimal.ZERO;
        BigDecimal totalDeathBenefit = BigDecimal.ZERO;
        Map<String, BigDecimal> assetAllocation = new HashMap<>();
        
        for (FinancialAccount.AssetClass ac : FinancialAccount.AssetClass.values()) {
            assetAllocation.put(ac.name(), BigDecimal.ZERO);
        }

        for (FinancialAccount acc : accounts) {
            BigDecimal bal = acc.getBalance() != null ? acc.getBalance() : BigDecimal.ZERO;
            if (acc.isLiability()) {
                totalLiabilities = totalLiabilities.add(bal.abs());
            } else {
                totalAssets = totalAssets.add(bal);
                String acName = acc.getAssetClass().name();
                assetAllocation.put(acName, assetAllocation.get(acName).add(bal));
            }
        }

        for (RecurringObligation obl : obligations) {
            if (obl.getDeathBenefitAmount() != null) {
                totalDeathBenefit = totalDeathBenefit.add(obl.getDeathBenefitAmount());
            }
        }

        BigDecimal monthlyInflow = BigDecimal.ZERO;
        for (IncomeSource inc : incomes) {
            if (inc.isActive()) {
                monthlyInflow = monthlyInflow.add(normalizeToMonthly(inc.getAmount(), inc.getFrequency()));
            }
        }

        BigDecimal monthlyOutflow = BigDecimal.ZERO;
        for (RecurringObligation obl : obligations) {
            monthlyOutflow = monthlyOutflow.add(normalizeToMonthly(obl.getAmount(), obl.getFrequency()));
        }

        BigDecimal monthlySurplus = monthlyInflow.subtract(monthlyOutflow);
        BigDecimal annualizedInflow = monthlyInflow.multiply(BigDecimal.valueOf(12));
        BigDecimal annualizedOutflow = monthlyOutflow.multiply(BigDecimal.valueOf(12));
        BigDecimal annualizedSurplus = monthlySurplus.multiply(BigDecimal.valueOf(12));
        BigDecimal savingsRate = BigDecimal.ZERO;
        if (monthlyInflow.compareTo(BigDecimal.ZERO) > 0) {
            savingsRate = monthlySurplus.multiply(BigDecimal.valueOf(100)).divide(monthlyInflow, 2, RoundingMode.HALF_UP);
        }

        metrics.put("totalAssets", totalAssets);
        metrics.put("totalLiabilities", totalLiabilities);
        metrics.put("netWorth", totalAssets.subtract(totalLiabilities));
        metrics.put("assetAllocation", assetAllocation);
        metrics.put("monthlyInflow", monthlyInflow);
        metrics.put("monthlyOutflow", monthlyOutflow);
        metrics.put("monthlySurplus", monthlySurplus);
        metrics.put("annualizedInflow", annualizedInflow);
        metrics.put("annualizedOutflow", annualizedOutflow);
        metrics.put("annualizedSurplus", annualizedSurplus);
        metrics.put("savingsRate", savingsRate);
        metrics.put("totalDeathBenefit", totalDeathBenefit);
        
        metrics.put("cashFlowForecast", projectionService.getForecast(1, userId));

        return metrics;
    }

    @GetMapping("/export-excel")
    public ResponseEntity<byte[]> exportExcel() throws IOException {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username).orElseThrow();
        byte[] excelContent = reportingService.generateFinancialExcelReport(user);
        String filename = "Finance_Summary_" + LocalDate.now() + ".xlsx";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelContent);
    }

    private BigDecimal normalizeToMonthly(BigDecimal amount, RecurringObligation.PaymentFrequency freq) {
        if (amount == null) return BigDecimal.ZERO;
        switch (freq) {
            case MONTHLY: return amount;
            case QUARTERLY: return amount.divide(BigDecimal.valueOf(3), 2, RoundingMode.HALF_UP);
            case YEARLY: return amount.divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP);
            default: return BigDecimal.ZERO;
        }
    }

    @GetMapping("/active-alerts")
    public List<Map<String, Object>> getActiveAlerts(@RequestParam(defaultValue = "30") int lookaheadDays) {
        Long userId = getCurrentUserId();
        List<Map<String, Object>> alerts = new ArrayList<>();
        LocalDate today = LocalDate.now();
        LocalDate limit = today.plusDays(lookaheadDays);

        List<IncomeSource> incomes = incomeRepository.findByUserId(userId);
        for (IncomeSource inc : incomes) {
            LocalDate due = inc.getNextExpectedDate();
            if (due != null && !due.isAfter(limit)) {
                alerts.add(createPendingAlert(inc.getName(), due, inc.getAmount(), "INCOME", inc.getId(), inc.getDestinationAccount()));
            }
        }

        List<RecurringObligation> obligations = obligationRepository.findByUserId(userId);
        for (RecurringObligation obl : obligations) {
            LocalDate due = obl.getNextDueDate();
            if (due != null && !due.isAfter(limit)) {
                alerts.add(createPendingAlert(obl.getInstrumentName(), due, obl.getAmount(), "OBLIGATION", obl.getId(), obl.getLinkedAccount()));
            }
        }
        return alerts;
    }

    private Map<String, Object> createPendingAlert(String name, LocalDate due, BigDecimal amount, String type, Long id, FinancialAccount linkedAccount) {
        Map<String, Object> alert = new HashMap<>();
        alert.put("id", id);
        alert.put("name", name);
        alert.put("nextDueDate", due.toString());
        alert.put("amount", amount);
        alert.put("type", type);
        if (linkedAccount != null) {
            alert.put("linkedAccountName", linkedAccount.getName());
            alert.put("accountBalance", linkedAccount.getBalance());
            if ("OBLIGATION".equals(type)) {
                boolean isSufficient = linkedAccount.getBalance().compareTo(amount) >= 0;
                alert.put("isSufficientFunds", isSufficient);
                alert.put("severity", isSufficient ? "INFO" : "CRITICAL");
            } else { alert.put("severity", "INFO"); }
        } else {
            alert.put("severity", "WARNING");
            alert.put("message", "No account linked");
        }
        return alert;
    }

    @PostMapping("/transactions/record-event")
    @Transactional
    public ResponseEntity<?> recordEvent(@RequestBody Map<String, Object> request) {
        Long userId = getCurrentUserId();
        String type = (String) request.get("type");
        Long id = Long.valueOf(request.get("id").toString());
        Transaction tx = new Transaction();
        tx.setUserId(userId);
        tx.setTransactionDate(LocalDate.now());

        if ("INCOME".equals(type)) {
            IncomeSource inc = incomeRepository.findById(id).orElseThrow();
            tx.setAmount(inc.getAmount());
            tx.setDestinationAccount(inc.getDestinationAccount());
            tx.setIncomeSourceId(id);
            tx.setDescription("Income received: " + inc.getName());
            tx.setType(Transaction.TransactionType.INCOME);
            updateAccountBalance(inc.getDestinationAccount(), inc.getAmount());
            inc.setNextExpectedDate(calculateNextDate(inc.getNextExpectedDate(), inc.getFrequency()));
            incomeRepository.save(inc);
        } else if ("OBLIGATION".equals(type)) {
            RecurringObligation obl = obligationRepository.findById(id).orElseThrow();
            tx.setAmount(obl.getAmount());
            tx.setSourceAccount(obl.getLinkedAccount());
            tx.setObligationId(id);
            tx.setDescription("Payment for " + obl.getInstrumentName());
            tx.setType(mapCategoryToTxType(obl.getCategory()));
            updateAccountBalance(obl.getLinkedAccount(), obl.getAmount().negate());
            obl.setNextDueDate(calculateNextDate(obl.getNextDueDate(), obl.getFrequency()));
            obligationRepository.save(obl);
        }
        transactionRepository.save(tx);
        return ResponseEntity.ok(tx);
    }

    @PostMapping("/transactions/manual")
    @Transactional
    public ResponseEntity<?> createManualTransaction(@RequestBody Map<String, Object> request) {
        Long userId = getCurrentUserId();
        Transaction tx = new Transaction();
        tx.setUserId(userId);
        tx.setTransactionDate(LocalDate.parse((String)request.get("date")));
        tx.setAmount(new BigDecimal(request.get("amount").toString()));
        tx.setDescription((String)request.get("description"));
        tx.setType(Transaction.TransactionType.valueOf((String)request.get("type")));

        if (request.get("sourceAccountId") != null && !request.get("sourceAccountId").toString().isEmpty()) {
            FinancialAccount src = accountRepository.findById(Long.valueOf(request.get("sourceAccountId").toString())).orElseThrow();
            tx.setSourceAccount(src);
            updateAccountBalance(src, tx.getAmount().negate());
        }
        if (request.get("destinationAccountId") != null && !request.get("destinationAccountId").toString().isEmpty()) {
            FinancialAccount dest = accountRepository.findById(Long.valueOf(request.get("destinationAccountId").toString())).orElseThrow();
            tx.setDestinationAccount(dest);
            updateAccountBalance(dest, tx.getAmount());
        }
        transactionRepository.save(tx);
        return ResponseEntity.ok(tx);
    }

    private void updateAccountBalance(FinancialAccount acc, BigDecimal delta) {
        if (acc != null) {
            acc.setBalance(acc.getBalance().add(delta));
            acc.setBalanceUpdatedDate(LocalDate.now());
            accountRepository.save(acc);
        }
    }

    private Transaction.TransactionType mapCategoryToTxType(RecurringObligation.ObligationCategory cat) {
        switch (cat) {
            case INVESTMENT_SIP: return Transaction.TransactionType.INVESTMENT;
            case LOAN_EMI: return Transaction.TransactionType.LOAN_REPAYMENT;
            default: return Transaction.TransactionType.EXPENSE;
        }
    }

    private LocalDate calculateNextDate(LocalDate current, RecurringObligation.PaymentFrequency freq) {
        if (current == null) return LocalDate.now();
        switch (freq) {
            case MONTHLY: return current.plusMonths(1);
            case QUARTERLY: return current.plusMonths(3);
            case YEARLY: return current.plusYears(1);
            default: return current;
        }
    }

    @GetMapping("/transactions") public List<Transaction> getRecentTransactions() { return transactionRepository.findByUserIdOrderByTransactionDateDesc(getCurrentUserId()); }
    @GetMapping("/forecast") public List<ProjectionService.CashFlowPoint> getForecast(@RequestParam(defaultValue = "15") int years) { return projectionService.getForecast(years, getCurrentUserId()); }
    @GetMapping("/accounts") public List<FinancialAccount> getAllAccounts() { return accountRepository.findByUserId(getCurrentUserId()); }
    @PostMapping("/accounts") public FinancialAccount createAccount(@RequestBody FinancialAccount a) { a.setUserId(getCurrentUserId()); return accountRepository.save(a); }
    @DeleteMapping("/accounts/{id}") public void deleteAccount(@PathVariable Long id) { accountRepository.deleteById(id); }
    @GetMapping("/income") public List<IncomeSource> getAllIncomes() { return incomeRepository.findByUserId(getCurrentUserId()); }
    @PostMapping("/income") public IncomeSource createIncome(@RequestBody IncomeSource inc) { inc.setUserId(getCurrentUserId()); return incomeRepository.save(inc); }
    @DeleteMapping("/income/{id}") public void deleteIncome(@PathVariable Long id) { incomeRepository.deleteById(id); }
    @GetMapping("/obligations") public List<RecurringObligation> getAllObligations() { return obligationRepository.findByUserId(getCurrentUserId()); }
    @PostMapping("/obligations")
    public RecurringObligation createObligation(@RequestBody Map<String, Object> payload) {
        Long userId = getCurrentUserId();
        RecurringObligation o = new RecurringObligation();
        try {
            if (payload.get("id") != null) o.setId(Long.valueOf(payload.get("id").toString()));
        } catch (Exception ignored) {}
        o.setUserId(userId);
        if (payload.get("instrumentName") != null) o.setInstrumentName(payload.get("instrumentName").toString());
        if (payload.get("institutionName") != null) o.setInstitutionName(payload.get("institutionName").toString());
        if (payload.get("referenceNo") != null) o.setReferenceNo(payload.get("referenceNo").toString());
        if (payload.get("nominee") != null) o.setNominee(payload.get("nominee").toString());
        try { if (payload.get("nextDueDate") != null && !payload.get("nextDueDate").toString().isBlank()) o.setNextDueDate(LocalDate.parse(payload.get("nextDueDate").toString())); } catch (Exception ignored) {}
        try { if (payload.get("endDate") != null && !payload.get("endDate").toString().isBlank()) o.setEndDate(LocalDate.parse(payload.get("endDate").toString())); } catch (Exception ignored) {}
        try { if (payload.get("amount") != null && !payload.get("amount").toString().isBlank()) o.setAmount(new BigDecimal(payload.get("amount").toString())); } catch (Exception ignored) {}
        try { if (payload.get("frequency") != null && !payload.get("frequency").toString().isBlank()) o.setFrequency(RecurringObligation.PaymentFrequency.valueOf(payload.get("frequency").toString())); } catch (Exception ignored) {}
        try { if (payload.get("category") != null && !payload.get("category").toString().isBlank()) o.setCategory(RecurringObligation.ObligationCategory.valueOf(payload.get("category").toString())); } catch (Exception ignored) {}
        try {
            if (payload.get("linkedAccount") != null && payload.get("linkedAccount") instanceof Map) {
                Object idv = ((Map)payload.get("linkedAccount")).get("id");
                if (idv != null && !idv.toString().isBlank()) {
                    Long accId = Long.valueOf(idv.toString());
                    accountRepository.findById(accId).ifPresent(o::setLinkedAccount);
                }
            }
        } catch (Exception ignored) {}
        try { if (payload.get("maturityIncomeAmount") != null && !payload.get("maturityIncomeAmount").toString().isBlank()) o.setMaturityIncomeAmount(new BigDecimal(payload.get("maturityIncomeAmount").toString())); } catch (Exception ignored) {}
        try { if (payload.get("maturityIncomeStartDate") != null && !payload.get("maturityIncomeStartDate").toString().isBlank()) o.setMaturityIncomeStartDate(LocalDate.parse(payload.get("maturityIncomeStartDate").toString())); } catch (Exception ignored) {}
        try { if (payload.get("maturityIncomeDurationYears") != null && !payload.get("maturityIncomeDurationYears").toString().isBlank()) o.setMaturityIncomeDurationYears(Integer.valueOf(payload.get("maturityIncomeDurationYears").toString())); } catch (Exception ignored) {}
        try { if (payload.get("maturityIncomeFrequency") != null && !payload.get("maturityIncomeFrequency").toString().isBlank()) o.setMaturityIncomeFrequency(RecurringObligation.PaymentFrequency.valueOf(payload.get("maturityIncomeFrequency").toString())); } catch (Exception ignored) {}
        try { if (payload.get("retirementInstrument") != null) o.setRetirementInstrument(parseBooleanValue(payload.get("retirementInstrument"))); } catch (Exception ignored) {}
        try { if (payload.get("deathBenefitAmount") != null && !payload.get("deathBenefitAmount").toString().isBlank()) o.setDeathBenefitAmount(new BigDecimal(payload.get("deathBenefitAmount").toString())); } catch (Exception ignored) {}
        try { if (payload.get("lumpSumMaturityAmount") != null && !payload.get("lumpSumMaturityAmount").toString().isBlank()) o.setLumpSumMaturityAmount(new BigDecimal(payload.get("lumpSumMaturityAmount").toString())); } catch (Exception ignored) {}
        try { if (payload.get("lumpSumMaturityDate") != null && !payload.get("lumpSumMaturityDate").toString().isBlank()) o.setLumpSumMaturityDate(LocalDate.parse(payload.get("lumpSumMaturityDate").toString())); } catch (Exception ignored) {}

        return obligationRepository.save(o);
    }
    @DeleteMapping("/obligations/{id}") public void deleteObligation(@PathVariable Long id) { obligationRepository.deleteById(id); }
    @GetMapping("/export") public ResponseEntity<String> exportData() throws IOException { return ResponseEntity.ok(dataService.exportDataToJson()); }
    @GetMapping("/retirement-projection")
    public ProjectionService.RetirementProjection getRetirementProjection(
            @RequestParam String fromMonth,
            @RequestParam String toMonth) {
        YearMonth from = YearMonth.parse(fromMonth);
        YearMonth to = YearMonth.parse(toMonth);
        return projectionService.getRetirementProjection(from, to, getCurrentUserId());
    }
    @PostMapping("/import") public ResponseEntity<String> importData(@RequestBody String json) { try { dataService.importDataFromJson(json); return ResponseEntity.ok("Import successful"); } catch (Exception e) { return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage()); } }

    private boolean parseBooleanValue(Object value) {
        if (value == null) return false;
        String str = value.toString().trim();
        return "true".equalsIgnoreCase(str) || "yes".equalsIgnoreCase(str) || "on".equalsIgnoreCase(str) || "1".equals(str);
    }
}