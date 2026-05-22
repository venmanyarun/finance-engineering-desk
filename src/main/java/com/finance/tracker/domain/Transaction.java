package com.finance.tracker.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
@Table(name = "transactions")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @NotNull
    private Long userId;

    @Column(nullable = false)
    @NotNull
    private LocalDate transactionDate;

    @Column(nullable = false, precision = 15, scale = 2)
    @NotNull
    private BigDecimal amount;

    @Column(nullable = false)
    @NotBlank
    private String description;

    @Enumerated(EnumType.STRING)
    private TransactionType type;

    @Enumerated(EnumType.STRING)
    private ExpenseCategory category;

    @ManyToOne
    @JoinColumn(name = "source_account_id")
    private FinancialAccount sourceAccount;

    @ManyToOne
    @JoinColumn(name = "destination_account_id")
    private FinancialAccount destinationAccount;

    // Optional links to track which obligation this transaction fulfilled
    private Long obligationId;
    private Long incomeSourceId;

    public enum TransactionType {
        INCOME, EXPENSE, TRANSFER, INVESTMENT, INSURANCE_PREMIUM, LOAN_REPAYMENT
    }

    public enum ExpenseCategory {
        FOOD, BILLS, TRANSPORT, HEALTH, ENTERTAINMENT, RENT, SHOPPING, GROCERIES, UTILITIES,
        HOUSEHOLD_EXPENSE, LOAN_EMI, INVESTMENT_SIP, TAX_PAYMENT, SUBSCRIPTION,
        GUARANTEED_RETURN, ULIP, HEALTH_INSURANCE, LIFE_INSURANCE, VEHICLE_INSURANCE,
        OTHER
    }

    public Transaction() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public LocalDate getTransactionDate() { return transactionDate; }
    public void setTransactionDate(LocalDate transactionDate) { this.transactionDate = transactionDate; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public TransactionType getType() { return type; }
    public void setType(TransactionType type) { this.type = type; }
    public ExpenseCategory getCategory() { return category; }
    public void setCategory(ExpenseCategory category) { this.category = category; }
    public FinancialAccount getSourceAccount() { return sourceAccount; }
    public void setSourceAccount(FinancialAccount sourceAccount) { this.sourceAccount = sourceAccount; }
    public FinancialAccount getDestinationAccount() { return destinationAccount; }
    public void setDestinationAccount(FinancialAccount destinationAccount) { this.destinationAccount = destinationAccount; }
    public Long getObligationId() { return obligationId; }
    public void setObligationId(Long obligationId) { this.obligationId = obligationId; }
    public Long getIncomeSourceId() { return incomeSourceId; }
    public void setIncomeSourceId(Long incomeSourceId) { this.incomeSourceId = incomeSourceId; }
}