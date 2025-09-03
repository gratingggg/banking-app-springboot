package com.example.bankingapp.dto.loan;

import com.example.bankingapp.entities.loan.LoanType;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public class LoanRequestDTO {
    @NotNull(message = "account cannot be null")
    private Long accountId;

    @NotNull(message = "issuance date cannot be null")
    @JsonFormat(pattern = "dd-MM-yyyy")
    private LocalDate dateOfIssuance;

    @NotNull(message = "tenure cannot be null")
    private Integer tenureInMonths;

    @NotNull(message = "loan type cannot be null")
    private LoanType loanType;

    @NotNull(message = "principal amount cannot be null")
    private BigDecimal principalAmount;

    @NotNull(message = "interest rate cannot be null")
    private BigDecimal rateOfInterest;

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public LocalDate getDateOfIssuance() {
        return dateOfIssuance;
    }

    public void setDateOfIssuance(LocalDate dateOfIssuance) {
        this.dateOfIssuance = dateOfIssuance;
    }

    public Integer getTenureInMonths() {
        return tenureInMonths;
    }

    public void setTenureInMonths(Integer tenureInMonths) {
        this.tenureInMonths = tenureInMonths;
    }

    public LoanType getLoanType() {
        return loanType;
    }

    public void setLoanType(LoanType loanType) {
        this.loanType = loanType;
    }

    public BigDecimal getPrincipalAmount() {
        return principalAmount;
    }

    public void setPrincipalAmount(BigDecimal principalAmount) {
        this.principalAmount = principalAmount;
    }

    public BigDecimal getRateOfInterest() {
        return rateOfInterest;
    }

    public void setRateOfInterest(BigDecimal rateOfInterest) {
        this.rateOfInterest = rateOfInterest;
    }


}
