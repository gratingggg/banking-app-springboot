package com.example.bankingapp.loan;

import com.example.bankingapp.account.Account;
import com.example.bankingapp.employee.Employee;
import com.example.bankingapp.entities.BaseEntity;
import com.example.bankingapp.transaction.Transaction;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.springframework.core.style.ToStringCreator;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Period;
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
    private BigDecimal principalAmount;

    @Column(name = "interest_rate", nullable = false)
    @NotNull(message = "interest rate cannot be null")
    private BigDecimal rateOfInterest;

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

    public BigDecimal getRateOfInterest() {
        return rateOfInterest;
    }

    public void setRateOfInterest(BigDecimal rateOfInterest) {
        this.rateOfInterest = rateOfInterest;
    }

    public BigDecimal getPrincipalAmount() {
        return principalAmount;
    }

    public void setPrincipalAmount(BigDecimal principalAmount) {
        this.principalAmount = principalAmount;
    }

    public Set<Transaction> getTransactions() {
        return transactions;
    }

    public synchronized void addTransaction(Transaction transaction){
        transactions.add(transaction);
        transaction.setLoan(this);
    }

    public Employee getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(Employee approvedBy) {
        this.loanStatus = LoanStatus.APPROVED;
        this.approvedBy = approvedBy;
    }

    public void rejected(){
        this.loanStatus = LoanStatus.REJECTED;
    }

    public boolean isOverdue(){
        if((loanStatus == LoanStatus.DEFAULTED) ||
                (loanStatus == LoanStatus.DISBURSED && LocalDate.now().isAfter(dateOfRepayment))){
            return true;
        }
        return false;
    }

    public BigDecimal getOutstandingAmount(){
        BigDecimal outstandingAmount = principalAmount;
        LocalDate currentDate = LocalDate.now();
        BigDecimal interest = BigDecimal.ZERO;
        if(!isOverdue()){
            long months = Period.between(dateOfIssuance, LocalDate.now()).toTotalMonths();
            interest = (BigDecimal.valueOf(months)
                    .divide(BigDecimal.valueOf(12), MathContext.DECIMAL128))
                    .multiply(rateOfInterest)
                    .multiply(principalAmount)
                    .divide(BigDecimal.valueOf(100), MathContext.DECIMAL128);
        }
        outstandingAmount = outstandingAmount.add(interest);
        for(Transaction transaction : transactions){
            if(transaction.isCredit()){
                outstandingAmount = outstandingAmount.subtract(transaction.getAmount());
            }
        }
        return outstandingAmount.setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal calculateEMI(){
        BigDecimal monthlyInterestRate = rateOfInterest.divide(BigDecimal.valueOf(1200), MathContext.DECIMAL128);
        BigDecimal partOfFormula = (monthlyInterestRate.add(BigDecimal.ONE)).pow(tenureInMonths);
        BigDecimal numerator = principalAmount.multiply(monthlyInterestRate).multiply(partOfFormula);
        BigDecimal denominator = partOfFormula.subtract(BigDecimal.ONE);
        BigDecimal result = numerator.divide(denominator, MathContext.DECIMAL128);
        return result.setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public String toString(){
        return new ToStringCreator(this)
                .append("id : ", getId())
                .append("type : ", getLoanType())
                .append("status : ", getLoanStatus())
                .append("repayment date : ", getDateOfRepayment())
                .append("tenure : ", getTenureInMonths())
                .append("amount : ", getPrincipalAmount())
                .append("interest : ", getRateOfInterest())
                .append("approved by : ", getApprovedBy().getName())
                .append("issuance date : " , getDateOfIssuance())
                .toString();
    }
}
