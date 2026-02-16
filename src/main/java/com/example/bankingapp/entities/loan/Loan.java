package com.example.bankingapp.entities.loan;

import com.example.bankingapp.entities.account.Account;
import com.example.bankingapp.entities.baseentities.BaseEntity;
import com.example.bankingapp.entities.employee.Employee;
import com.example.bankingapp.entities.transaction.Transaction;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.springframework.core.style.ToStringCreator;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Period;
import java.util.*;

@Entity
@Table(name = "loans")
public class Loan extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false)
    @NotNull(message = "account cannot be null")
    private Account account;

    @Column(name = "issuance_date")
    @DateTimeFormat(pattern = "dd-MM-yyyy")
    private LocalDate dateOfIssuance;

    @Column(name = "tenure", nullable = false)
    @NotNull(message = "tenure cannot be null")
    private Integer tenureInMonths;

    @Transient
    private LocalDate dateOfRepayment;

    @Column(name = "type", nullable = false)
    @NotNull(message = "loan type cannot be null.")
    @Enumerated(EnumType.STRING)
    private LoanType loanType;

    @Column(name = "status", nullable = false)
    @NotNull(message = "Loan status cannot be null.")
    @Enumerated(EnumType.STRING)
    private LoanStatus loanStatus;

    @Column(name = "principal_amount", nullable = false)
    @NotNull(message = "principal amount cannot be null.")
    private BigDecimal principalAmount;

    @Column(name = "interest_rate", nullable = false)
    @NotNull(message = "interest rate cannot be null.")
    private BigDecimal rateOfInterest;

    @OneToMany(mappedBy = "loan", fetch = FetchType.LAZY,
    cascade = CascadeType.ALL, orphanRemoval = true)
    private final Set<Transaction> transactions = new LinkedHashSet<>();

    @ManyToOne
    @JoinColumn(name = "employee_id")
    private Employee handledBy;

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
        return handledBy;
    }

    public void setApprovedBy(Employee approvedBy) {
        this.loanStatus = LoanStatus.APPROVED;
        this.handledBy = approvedBy;
    }

    public void rejected(){
        this.loanStatus = LoanStatus.REJECTED;
    }

    public boolean isOverdue(){
        LocalDate repaymentDate = getDateOfRepayment();
        if(repaymentDate == null){
            return false;
        }
        return (loanStatus == LoanStatus.DEFAULTED) ||
                (loanStatus == LoanStatus.DISBURSED && LocalDate.now().isAfter(repaymentDate));
    }

    public BigDecimal getOutstandingAmount(){
        BigDecimal monthlyRate = rateOfInterest.divide(BigDecimal.valueOf(1200), MathContext.DECIMAL128);
        BigDecimal emi = calculateEMI();
        BigDecimal balance = principalAmount;

        Map<Integer, BigDecimal> paymentsByMonth = getPaymentsByMonth();

        long monthsToProcess = Period.between(dateOfIssuance, LocalDate.now()).toTotalMonths();
        if (monthsToProcess == 0 && !paymentsByMonth.isEmpty()) {
            monthsToProcess = 1;
        }

        for (int month = 1; month <= monthsToProcess; month++) {
            BigDecimal interest = balance.multiply(monthlyRate);
            balance = balance.add(interest);

            BigDecimal payment = paymentsByMonth.getOrDefault(month, BigDecimal.ZERO);
            balance = balance.subtract(payment);

            if (month <= tenureInMonths && payment.compareTo(emi) < 0) {
                BigDecimal shortfall = emi.subtract(payment);
                BigDecimal penalty = shortfall.multiply(BigDecimal.valueOf(0.02));
                balance = balance.add(penalty);
            }
        }

        return balance.max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
    }

    private Map<Integer, BigDecimal> getPaymentsByMonth() {
        Map<Integer, BigDecimal> paymentsByMonth = new HashMap<>();

        List<Transaction> sortedTransactions = transactions.stream()
                .filter(Transaction::isDebit)
                .sorted(Comparator.comparing(Transaction::getDateOfTransaction))
                .toList();

        for (Transaction transaction : sortedTransactions) {
            long monthNumber = Period.between(dateOfIssuance, transaction.getDateOfTransaction().toLocalDate()).toTotalMonths() + 1;

            if(monthNumber > 0) {
                paymentsByMonth.merge(
                        (int) monthNumber,
                        transaction.getAmount(),
                        BigDecimal::add
                );
            }
        }

        return paymentsByMonth;
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