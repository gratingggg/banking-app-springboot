package com.example.bankingapp.loan;

import com.example.bankingapp.account.Account;
import com.example.bankingapp.employee.Employee;
import com.example.bankingapp.entities.BaseEntity;
import com.example.bankingapp.transaction.Transaction;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "loans")
public class Loan extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false)
    @NotNull(message = "account cannot be null")
    private Account account;

    @Column(name = "issuance_date", nullable = false)
    @NotNull(message = "issuance date cannot be null")
    @DateTimeFormat(pattern = "dd-MM-yyyy")
    private LocalDate dateOfIssuance;

    @Column(name = "tenure", nullable = false)
    @NotNull(message = "tenure cannot be null")
    private Integer tenureInMonths;

    @Transient
    private LocalDate dateOfRepayment;

    @Column(name = "type", nullable = false)
    @NotNull(message = "loan type cannot be null")
    @Enumerated(EnumType.STRING)
    private LoanType loanType;

    @Column(name = "status", nullable = false)
    @NotNull(message = "Loan status cannot be null")
    @Enumerated(EnumType.STRING)
    private LoanStatus loanStatus;

    @Column(name = "principal_amount", nullable = false)
    @NotNull(message = "principal amount cannot be null")
    private Long principalAmount;

    @Column(name = "interest_rate", nullable = false)
    @NotNull(message = "interest rate cannot be null")
    private Double rateOfInterest;

    @OneToMany(mappedBy = "loan", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private final Set<Transaction> transactions = new LinkedHashSet<>();

    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    @NotNull(message = "Employee cannot be null")
    private Employee approvedBy;

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
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
        if(dateOfIssuance != null && tenureInMonths != null){
            return dateOfIssuance.plusMonths(tenureInMonths);
        }
        return null;
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

    public Double getRateOfInterest() {
        return rateOfInterest;
    }

    public void setRateOfInterest(Double rateOfInterest) {
        this.rateOfInterest = rateOfInterest;
    }

    public Long getPrincipalAmount() {
        return principalAmount;
    }

    public void setPrincipalAmount(Long principalAmount) {
        this.principalAmount = principalAmount;
    }

    public Set<Transaction> getTransactions() {
        return transactions;
    }

    public void addTransaction(Transaction transaction){
        transactions.add(transaction);
        transaction.setLoan(this);
    }

    public Employee getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(Employee approvedBy) {
        this.approvedBy = approvedBy;
    }
}
