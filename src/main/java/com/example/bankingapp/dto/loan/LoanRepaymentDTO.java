package com.example.bankingapp.dto.loan;

import java.math.BigDecimal;

public class LoanRepaymentDTO {
    private Long loanId;
    private BigDecimal amount;

    public Long getLoanId() {
        return loanId;
    }

    public void setLoanId(Long loanId) {
        this.loanId = loanId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
