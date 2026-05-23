package com.finance.tracker.service;

import com.finance.tracker.domain.*;
import com.finance.tracker.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

@Service
public class ProjectionService {

    private static final Logger log = LoggerFactory.getLogger(ProjectionService.class);

    @Autowired
    private IncomeSourceRepository incomeRepository;
    @Autowired
    private RecurringObligationRepository obligationRepository;
    @Autowired
    private FinancialAccountRepository accountRepository;

    public static class MonthlySnapshot {
        public String month;
        public BigDecimal totalInflow = BigDecimal.ZERO;
        public BigDecimal totalOutflow = BigDecimal.ZERO;
        public BigDecimal netSavings = BigDecimal.ZERO;
        public List<String> events = new ArrayList<>();

        public MonthlySnapshot(String month) { this.month = month; }
    }

    public static class CashFlowPoint {
        public String date;
        public BigDecimal netInflow = BigDecimal.ZERO;
        public BigDecimal netOutflow = BigDecimal.ZERO;
        public String description;

        public CashFlowPoint() {}

        public CashFlowPoint(String date, BigDecimal netInflow, BigDecimal netOutflow, String description) {
            this.date = date;
            this.netInflow = netInflow;
            this.netOutflow = netOutflow;
            this.description = description;
        }
    }

    public static class RetirementProjection {
        public BigDecimal retirementAccountBalance = BigDecimal.ZERO;
        public BigDecimal projectedRecurringIncome = BigDecimal.ZERO;
        public BigDecimal projectedLumpSum = BigDecimal.ZERO;
        public BigDecimal totalProjectedRetirement = BigDecimal.ZERO;
        public List<Map<String, Object>> retirementAccounts = new ArrayList<>();
        public List<Map<String, Object>> retirementObligations = new ArrayList<>();
    }

    public RetirementProjection getRetirementProjection(YearMonth fromMonth, YearMonth toMonth, Long userId) {
        log.info("getRetirementProjection called for userId: {}, fromMonth: {}, toMonth: {}", userId, fromMonth, toMonth);
        YearMonth startYm = fromMonth.compareTo(toMonth) <= 0 ? fromMonth : toMonth;
        YearMonth endYm = fromMonth.compareTo(toMonth) <= 0 ? toMonth : fromMonth;
        LocalDate windowStart = startYm.atDay(1);
        LocalDate windowEnd = endYm.atEndOfMonth();

        RetirementProjection projection = new RetirementProjection();

        List<FinancialAccount> accounts = accountRepository.findByUserId(userId);
        for (FinancialAccount account : accounts) {
            if (account.isRetirementAsset()) {
                BigDecimal bal = account.getBalance() != null ? account.getBalance() : BigDecimal.ZERO;
                projection.retirementAccountBalance = projection.retirementAccountBalance.add(bal);
                Map<String, Object> accountDetail = new HashMap<>();
                accountDetail.put("name", account.getName());
                accountDetail.put("balance", bal);
                accountDetail.put("institution", account.getInstitution());
                projection.retirementAccounts.add(accountDetail);
            }
        }

        List<RecurringObligation> obligations = obligationRepository.findByUserId(userId);
        for (RecurringObligation obligation : obligations) {
            if (!obligation.isRetirementInstrument()) continue;
            BigDecimal incomeTotal = BigDecimal.ZERO;
            BigDecimal lumpTotal = BigDecimal.ZERO;
            Map<String, Object> obligationDetail = new HashMap<>();
            obligationDetail.put("instrumentName", obligation.getInstrumentName());
            obligationDetail.put("category", obligation.getCategory());

            if (obligation.getMaturityIncomeAmount() != null && obligation.getMaturityIncomeAmount().compareTo(BigDecimal.ZERO) > 0 && obligation.getMaturityIncomeStartDate() != null) {
                LocalDate current = obligation.getMaturityIncomeStartDate();
                LocalDate maturityEnd = obligation.getMaturityIncomeStartDate().plusYears(obligation.getMaturityIncomeDurationYears() != null ? obligation.getMaturityIncomeDurationYears() : 0);
                while (!current.isAfter(windowEnd)) {
                    if (!current.isBefore(windowStart) && (maturityEnd == null || !current.isAfter(maturityEnd))) {
                        incomeTotal = incomeTotal.add(obligation.getMaturityIncomeAmount());
                    }
                    if (obligation.getMaturityIncomeFrequency() == RecurringObligation.PaymentFrequency.ONE_TIME) break;
                    current = advanceDate(current, obligation.getMaturityIncomeFrequency());
                    if (maturityEnd != null && current.isAfter(maturityEnd)) break;
                }
            }

            if (obligation.getLumpSumMaturityAmount() != null && obligation.getLumpSumMaturityDate() != null) {
                LocalDate payout = obligation.getLumpSumMaturityDate();
                if (!payout.isBefore(windowStart) && !payout.isAfter(windowEnd)) {
                    lumpTotal = lumpTotal.add(obligation.getLumpSumMaturityAmount());
                }
            }

            if (incomeTotal.compareTo(BigDecimal.ZERO) > 0 || lumpTotal.compareTo(BigDecimal.ZERO) > 0) {
                obligationDetail.put("projectedRecurringIncome", incomeTotal);
                obligationDetail.put("projectedLumpSum", lumpTotal);
                projection.projectedRecurringIncome = projection.projectedRecurringIncome.add(incomeTotal);
                projection.projectedLumpSum = projection.projectedLumpSum.add(lumpTotal);
                projection.retirementObligations.add(obligationDetail);
            }
        }

        projection.totalProjectedRetirement = projection.retirementAccountBalance
                .add(projection.projectedRecurringIncome)
                .add(projection.projectedLumpSum);
        return projection;
    }

    public List<CashFlowPoint> getForecast(int years, Long userId) {
        log.info("getForecast called for userId: {}, years: {}", userId, years);
        // We simulate with a 1-month buffer to handle boundaries, then return the next 12 full months.
        List<MonthlySnapshot> snapshots = getMonthlyProjection(years * 12 + 1, userId);
        List<CashFlowPoint> forecast = new ArrayList<>();
        for (int i = 1; i <= years * 12 && i < snapshots.size(); i++) {
            MonthlySnapshot snap = snapshots.get(i);
            String desc = String.join(", ", snap.events);
            forecast.add(new CashFlowPoint(snap.month, snap.totalInflow, snap.totalOutflow, desc));
        }
        return forecast;
    }

    public List<MonthlySnapshot> getMonthlyProjection(int monthsAhead, Long userId) {
        log.info("getMonthlyProjection called for userId: {}, monthsAhead: {}", userId, monthsAhead);
        Map<YearMonth, MonthlySnapshot> projectionMap = new LinkedHashMap<>();
        YearMonth startMonth = YearMonth.now();

        for (int i = 0; i < monthsAhead; i++) {
            YearMonth targetMonth = startMonth.plusMonths(i);
            projectionMap.put(targetMonth, new MonthlySnapshot(targetMonth.toString()));
        }

        YearMonth endWindow = startMonth.plusMonths(monthsAhead - 1);

        // 1. Process Income
        List<IncomeSource> incomes = incomeRepository.findByUserId(userId);
        for (IncomeSource income : incomes) {
            if (!income.isActive()) continue;
            LocalDate flowStart = income.getNextExpectedDate() != null ? income.getNextExpectedDate() : income.getStartDate();
            applyFlow(projectionMap, flowStart, null, income.getFrequency(), income.getAmount(), true, "Inflow: " + income.getName(), startMonth, endWindow);
        }

        // 2. Process Obligations
        List<RecurringObligation> obligations = obligationRepository.findByUserId(userId);
        for (RecurringObligation obl : obligations) {
            // Main Outflow
            applyFlow(projectionMap, obl.getNextDueDate(), obl.getEndDate(), obl.getFrequency(), obl.getAmount(), false, "Outflow: " + obl.getInstrumentName(), startMonth, endWindow);

            // Future Recurring Inflow
            if (obl.getMaturityIncomeAmount() != null && obl.getMaturityIncomeAmount().compareTo(BigDecimal.ZERO) > 0 && obl.getMaturityIncomeStartDate() != null) {
                LocalDate maturityEnd = null;
                if (obl.getMaturityIncomeDurationYears() != null) {
                    maturityEnd = obl.getMaturityIncomeStartDate().plusYears(obl.getMaturityIncomeDurationYears());
                }
                applyFlow(projectionMap, obl.getMaturityIncomeStartDate(), maturityEnd, obl.getMaturityIncomeFrequency(), obl.getMaturityIncomeAmount(), true, "Return: " + obl.getInstrumentName(), startMonth, endWindow);
            }

            // Future One-time Maturity
            if (obl.getLumpSumMaturityAmount() != null && obl.getLumpSumMaturityDate() != null) {
                YearMonth payoutMonth = YearMonth.from(obl.getLumpSumMaturityDate());
                if (projectionMap.containsKey(payoutMonth)) {
                    MonthlySnapshot snap = projectionMap.get(payoutMonth);
                    snap.totalInflow = snap.totalInflow.add(obl.getLumpSumMaturityAmount());
                    snap.events.add("Maturity: " + obl.getInstrumentName() + " (₹" + obl.getLumpSumMaturityAmount() + ")");
                }
            }
        }

        projectionMap.values().forEach(snap -> snap.netSavings = snap.totalInflow.subtract(snap.totalOutflow));
        return new ArrayList<>(projectionMap.values());
    }

    private void applyFlow(Map<YearMonth, MonthlySnapshot> map, LocalDate startDate, LocalDate endDate, RecurringObligation.PaymentFrequency freq, BigDecimal amount, boolean isInflow, String desc, YearMonth windowStart, YearMonth windowEnd) {
        if (startDate == null || freq == null || amount == null) return;
        
        LocalDate current = startDate;

        // Alignment logic: find the first occurrence within or after windowStart
        if (YearMonth.from(current).isBefore(windowStart)) {
            if (freq == RecurringObligation.PaymentFrequency.ONE_TIME) return;
            while (YearMonth.from(current).isBefore(windowStart)) {
                current = advanceDate(current, freq);
            }
        }

        // Fill within window boundaries and before endDate
        while (!YearMonth.from(current).isAfter(windowEnd)) {
            if (endDate != null && current.isAfter(endDate)) break;
            
            YearMonth ym = YearMonth.from(current);
            if (map.containsKey(ym)) {
                MonthlySnapshot snap = map.get(ym);
                if (isInflow) snap.totalInflow = snap.totalInflow.add(amount);
                else snap.totalOutflow = snap.totalOutflow.add(amount);
                snap.events.add(desc + " (₹" + amount + ")");
            }

            if (freq == RecurringObligation.PaymentFrequency.ONE_TIME) break;
            current = advanceDate(current, freq);
        }
    }

    private LocalDate advanceDate(LocalDate date, RecurringObligation.PaymentFrequency freq) {
        return switch (freq) {
            case QUARTERLY -> date.plusMonths(3);
            case YEARLY -> date.plusYears(1);
            case ONE_TIME -> date;
            default -> date.plusMonths(1);
        };
    }
}