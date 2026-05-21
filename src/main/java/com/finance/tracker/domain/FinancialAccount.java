package com.finance.tracker.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
@Table(name = "financial_accounts")
public class FinancialAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @NotNull
    private Long userId; // Linked to User in authdb by ID

    @Column(nullable = false)
    @NotBlank
    @Size(min = 2, max = 100)
    private String name;

    @Column(nullable = false)
    @NotBlank
    private String institution;

    private String accountNo;
    private String nominee;

    @Column(nullable = false, precision = 15, scale = 2)
    @NotNull
    private BigDecimal balance;

    @Column(nullable = false)
    @NotNull
    private LocalDate balanceUpdatedDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull
    private AssetClass assetClass;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull
    private AccountType accountType;

    private LocalDate maturityDate;
    @Column(precision = 15, scale = 2)
    private BigDecimal maturityAmount;
    private Double interestRate;

    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean retirementAsset = false;

    @Column(precision = 15, scale = 2)
    private BigDecimal emiAmount;
    private Integer totalTenureMonths;
    private Integer remainingTenureMonths;

    public enum AssetClass {
        CASH_EQUIVALENTS, FIXED_INCOME, EQUITIES, RETIREMENT, REAL_ESTATE, LIABILITIES
    }

    public enum AccountType {
        SAVINGS_ACCOUNT, CURRENT_ACCOUNT, FIXED_DEPOSIT, RECURRING_DEPOSIT, MUTUAL_FUND, EQUITY_STOCKS, PROVIDENT_FUND, PUBLIC_PROVIDENT_FUND, NPS, HOME_LOAN, PERSONAL_LOAN, VEHICLE_LOAN, CREDIT_CARD, CASH_WALLET
    }

    public FinancialAccount() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getInstitution() { return institution; }
    public void setInstitution(String institution) { this.institution = institution; }
    public String getAccountNo() { return accountNo; }
    public void setAccountNo(String accountNo) { this.accountNo = accountNo; }
    public String getNominee() { return nominee; }
    public void setNominee(String nominee) { this.nominee = nominee; }
    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }
    public LocalDate getBalanceUpdatedDate() { return balanceUpdatedDate; }
    public void setBalanceUpdatedDate(LocalDate balanceUpdatedDate) { this.balanceUpdatedDate = balanceUpdatedDate; }
    public AssetClass getAssetClass() { return assetClass; }
    public void setAssetClass(AssetClass assetClass) { this.assetClass = assetClass; }
    public AccountType getAccountType() { return accountType; }
    public void setAccountType(AccountType accountType) { this.accountType = accountType; }
    public LocalDate getMaturityDate() { return maturityDate; }
    public void setMaturityDate(LocalDate maturityDate) { this.maturityDate = maturityDate; }
    public BigDecimal getMaturityAmount() { return maturityAmount; }
    public void setMaturityAmount(BigDecimal maturityAmount) { this.maturityAmount = maturityAmount; }
    public Double getInterestRate() { return interestRate; }
    public void setInterestRate(Double interestRate) { this.interestRate = interestRate; }
    public boolean isRetirementAsset() { return retirementAsset; }
    public void setRetirementAsset(boolean retirementAsset) { this.retirementAsset = retirementAsset; }
    public BigDecimal getEmiAmount() { return emiAmount; }
    public void setEmiAmount(BigDecimal emiAmount) { this.emiAmount = emiAmount; }
    public Integer getTotalTenureMonths() { return totalTenureMonths; }
    public void setTotalTenureMonths(Integer totalTenureMonths) { this.totalTenureMonths = totalTenureMonths; }
    public Integer getRemainingTenureMonths() { return remainingTenureMonths; }
    public void setRemainingTenureMonths(Integer remainingTenureMonths) { this.remainingTenureMonths = remainingTenureMonths; }
    
    public boolean isLiability() {
        return this.assetClass == AssetClass.LIABILITIES;
    }
}