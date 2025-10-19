package com.example.bankingapp.dto.loan;

import com.example.bankingapp.entities.loan.Loan;
import com.example.bankingapp.entities.loan.LoanStatus;
import com.example.bankingapp.entities.loan.LoanType;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

public class LoanResponseDTO {
    private Long accountId;

    private Long loanId;

    @JsonFormat(pattern = "dd-MM-yyyy")
    private LocalDate dateOfIssuance;

    private Integer tenureInMonths;

    private LocalDate dateOfRepayment;

    private LoanType loanType;

    private LoanStatus loanStatus;

    private BigDecimal principalAmount;

    private BigDecimal rateOfInterest;

    private BigDecimal emi;

    public LoanResponseDTO(Loan loan){
        accountId = loan.getAccount().getId();
        loanId = loan.getId();
        dateOfIssuance = loan.getDateOfIssuance();
        tenureInMonths = loan.getTenureInMonths();
        dateOfRepayment = loan.getDateOfRepayment();
        loanType = loan.getLoanType();
        loanStatus = loan.getLoanStatus();
        principalAmount = loan.getPrincipalAmount();
        rateOfInterest = loan.getRateOfInterest();
        emi = loan.calculateEMI();
    }

    public BigDecimal getEmi() {
        return emi;
    }

    public void setEmi(BigDecimal emi) {
        this.emi = emi;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public Long getLoanId() {
        return loanId;
    }

    public void setLoanId(Long loanId) {
        this.loanId = loanId;
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
