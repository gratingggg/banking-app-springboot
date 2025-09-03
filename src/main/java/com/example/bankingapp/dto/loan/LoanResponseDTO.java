package com.example.bankingapp.dto.loan;

import com.example.bankingapp.entities.loan.LoanStatus;
import com.example.bankingapp.entities.loan.LoanType;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

public class LoanResponseDTO {
    private Long accountId;

    @JsonFormat(pattern = "dd-MM-yyyy")
    private LocalDate dateOfIssuance;

    private Integer tenureInMonths;

    private LocalDate dateOfRepayment;

    private LoanType loanType;

    private LoanStatus loanStatus;

    private BigDecimal principalAmount;

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

    public LocalDate getDateOfRepayment() {
        return dateOfRepayment;
    }

    public void setDateOfRepayment(LocalDate dateOfRepayment) {
        this.dateOfRepayment = dateOfRepayment;
    }

    public LoanType getLoanType() {
        return loanType;
    }

    public void setLoanType(LoanType loanType) {
        this.loanType = loanType;
    }

    public LoanStatus getLoanStatus() {
        return loanStatus;
    }

    public void setLoanStatus(LoanStatus loanStatus) {
        this.loanStatus = loanStatus;
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
