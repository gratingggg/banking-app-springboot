package com.example.bankingapp.employee;

import com.example.bankingapp.entities.Person;
import com.example.bankingapp.loan.Loan;
import com.example.bankingapp.transaction.Transaction;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.core.style.ToStringCreator;

import java.util.*;

@Entity
@Table(name = "employees")
public class Employee extends Person {
    @Column(name = "role", nullable = false)
    @NotNull(message = "Role cannot be null")
    @Enumerated(EnumType.STRING)
    private EmployeeRole employeeRole;

    @Column(name = "username", nullable = false)
    @NotBlank(message = "Username cannot be blank")
    private String username;

    @Column(name = "password", nullable = false)
    @NotBlank(message = "Password cannot be blank")
    private String password;

    @Column(name = "status", nullable = false)
    @NotNull(message = "Employee status cannot be null")
    @Enumerated(EnumType.STRING)
    private EmployeeStatus employeeStatus;

    @OneToMany(mappedBy = "approvedBy")
    private final Set<Loan> approvedLoans = new LinkedHashSet<>();

    @OneToMany(mappedBy = "handledBy")
    private final Set<Transaction> handledTransactions = new LinkedHashSet<>();

    public EmployeeRole getEmployeeRole() {
        return employeeRole;
    }

    public void setEmployeeRole(EmployeeRole employeeRole) {
        this.employeeRole = employeeRole;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public EmployeeStatus getEmployeeStatus() {
        return employeeStatus;
    }

    public void setEmployeeStatus(EmployeeStatus employeeStatus) {
        this.employeeStatus = employeeStatus;
    }

    public Set<Loan> getApprovedLoans() {
        return approvedLoans;
    }

    public Set<Transaction> getHandledTransactions() {
        return handledTransactions;
    }

    public void addApprovedLoan(Loan loan){
        approvedLoans.add(loan);
        loan.setApprovedBy(this);
    }

    public void addHandledTransaction(Transaction transaction){
        handledTransactions.add(transaction);
        transaction.setHandledBy(this);
    }

    @Override
    public String toString(){
        return new ToStringCreator(this)
                .append("id : ", this.getId())
                .append("name : ", this.getName())
                .append("phone number : ", this.getPhone_number())
                .append("birth date : ", this.getDateOfBirth())
                .append("address : ", this.getAddress())
                .append("gender : ", this.getGender())
                .append("username : ", this.getUsername())
                .append("role : ", this.getEmployeeRole())
                .append("status : ", this.getEmployeeStatus())
                .toString();
    }
}
