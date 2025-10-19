package com.example.bankingapp.dto.loan;

import com.example.bankingapp.entities.loan.LoanType;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class LoanRequestDTO {
    @NotNull(message = "account cannot be null")
    private Long accountId;

    @NotNull(message = "tenure cannot be null")
    private Integer tenureInMonths;

    @NotNull(message = "loan type cannot be null")
    private LoanType loanType;

    @NotNull(message = "principal amount cannot be null")
    private BigDecimal principalAmount;

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
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
}
