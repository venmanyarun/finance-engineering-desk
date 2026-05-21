package com.finance.tracker.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
@Table(name = "recurring_obligations")
public class RecurringObligation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @NotNull
    private Long userId;

    @NotBlank
    private String instrumentName;

    private String institutionName;
    private String referenceNo;
    private String nominee; 

    @NotNull
    private LocalDate nextDueDate;

    private LocalDate endDate; 

    @NotNull
    @Positive
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @NotNull
    private PaymentFrequency frequency;

    @Enumerated(EnumType.STRING)
    @NotNull
    private ObligationCategory category;

    @ManyToOne
    @JoinColumn(name = "linked_account_id")
    private FinancialAccount linkedAccount;

    // Guaranteed Returns / Survival Benefits
    private BigDecimal maturityIncomeAmount; 
    private LocalDate maturityIncomeStartDate;
    private Integer maturityIncomeDurationYears;
    
    @Enumerated(EnumType.STRING)
    private PaymentFrequency maturityIncomeFrequency;

    // Insurance Specifics
    private BigDecimal deathBenefitAmount; 
    private BigDecimal lumpSumMaturityAmount; 
    private LocalDate lumpSumMaturityDate;

    public enum PaymentFrequency {
        MONTHLY(12), QUARTERLY(4), YEARLY(1), ONE_TIME(0);
        private final int factor;
        PaymentFrequency(int factor) { this.factor = factor; }
        public int getFactor() { return factor; }
    }

    public enum ObligationCategory {
        HOUSEHOLD_EXPENSE,
        LOAN_EMI,
        INVESTMENT_SIP,
        TAX_PAYMENT,
        SUBSCRIPTION,
        OTHER,
        GUARANTEED_RETURN,
        ULIP,
        HEALTH_INSURANCE,
        LIFE_INSURANCE,
        VEHICLE_INSURANCE
    }

    public RecurringObligation() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getInstrumentName() { return instrumentName; }
    public void setInstrumentName(String instrumentName) { this.instrumentName = instrumentName; }
    public String getInstitutionName() { return institutionName; }
    public void setInstitutionName(String institutionName) { this.institutionName = institutionName; }
    public String getReferenceNo() { return referenceNo; }
    public void setReferenceNo(String referenceNo) { this.referenceNo = referenceNo; }
    public String getNominee() { return nominee; }
    public void setNominee(String nominee) { this.nominee = nominee; }
    public LocalDate getNextDueDate() { return nextDueDate; }
    public void setNextDueDate(LocalDate nextDueDate) { this.nextDueDate = nextDueDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public PaymentFrequency getFrequency() { return frequency; }
    public void setFrequency(PaymentFrequency frequency) { this.frequency = frequency; }
    public ObligationCategory getCategory() { return category; }
    public void setCategory(ObligationCategory category) { this.category = category; }
    public FinancialAccount getLinkedAccount() { return linkedAccount; }
    public void setLinkedAccount(FinancialAccount linkedAccount) { this.linkedAccount = linkedAccount; }
    public BigDecimal getMaturityIncomeAmount() { return maturityIncomeAmount; }
    public void setMaturityIncomeAmount(BigDecimal maturityIncomeAmount) { this.maturityIncomeAmount = maturityIncomeAmount; }
    public LocalDate getMaturityIncomeStartDate() { return maturityIncomeStartDate; }
    public void setMaturityIncomeStartDate(LocalDate maturityIncomeStartDate) { this.maturityIncomeStartDate = maturityIncomeStartDate; }
    public Integer getMaturityIncomeDurationYears() { return maturityIncomeDurationYears; }
    public void setMaturityIncomeDurationYears(Integer maturityIncomeDurationYears) { this.maturityIncomeDurationYears = maturityIncomeDurationYears; }
    public PaymentFrequency getMaturityIncomeFrequency() { return maturityIncomeFrequency; }
    public void setMaturityIncomeFrequency(PaymentFrequency maturityIncomeFrequency) { this.maturityIncomeFrequency = maturityIncomeFrequency; }
    public BigDecimal getDeathBenefitAmount() { return deathBenefitAmount; }
    public void setDeathBenefitAmount(BigDecimal deathBenefitAmount) { this.deathBenefitAmount = deathBenefitAmount; }
    public BigDecimal getLumpSumMaturityAmount() { return lumpSumMaturityAmount; }
    public void setLumpSumMaturityAmount(BigDecimal lumpSumMaturityAmount) { this.lumpSumMaturityAmount = lumpSumMaturityAmount; }
    public LocalDate getLumpSumMaturityDate() { return lumpSumMaturityDate; }
    public void setLumpSumMaturityDate(LocalDate lumpSumMaturityDate) { this.lumpSumMaturityDate = lumpSumMaturityDate; }
}