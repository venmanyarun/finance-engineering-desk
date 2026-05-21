package com.finance.tracker.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
@Table(name = "income_sources")
public class IncomeSource {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @NotNull
    private Long userId;

    @NotBlank
    private String name;

    private String institutionName; // e.g., Employer or Payer

    @NotNull
    @Positive
    private BigDecimal amount;
    
    @Enumerated(EnumType.STRING)
    @NotNull
    private RecurringObligation.PaymentFrequency frequency;

    @NotNull
    private LocalDate startDate;
    
    private LocalDate nextExpectedDate;
    private boolean active = true;

    @ManyToOne
    @JoinColumn(name = "destination_account_id")
    private FinancialAccount destinationAccount;

    public IncomeSource() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getInstitutionName() { return institutionName; }
    public void setInstitutionName(String institutionName) { this.institutionName = institutionName; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public RecurringObligation.PaymentFrequency getFrequency() { return frequency; }
    public void setFrequency(RecurringObligation.PaymentFrequency frequency) { this.frequency = frequency; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getNextExpectedDate() { return nextExpectedDate; }
    public void setNextExpectedDate(LocalDate nextExpectedDate) { this.nextExpectedDate = nextExpectedDate; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public FinancialAccount getDestinationAccount() { return destinationAccount; }
    public void setDestinationAccount(FinancialAccount destinationAccount) { this.destinationAccount = destinationAccount; }
}