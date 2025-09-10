package com.example.bankingapp.entities.employee;

import com.example.bankingapp.Role;
import com.example.bankingapp.entities.baseentities.Person;
import com.example.bankingapp.entities.loan.Loan;
import com.example.bankingapp.entities.transaction.Transaction;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.springframework.core.style.ToStringCreator;

import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "employees")
public class Employee extends Person {
    @Column(name = "employee_role", nullable = false)
    @NotNull(message = "Employee role cannot be null")
    @Enumerated(EnumType.STRING)
    private EmployeeRole employeeRole;

    @Column(name = "status", nullable = false)
    @NotNull(message = "Employee status cannot be null")
    @Enumerated(EnumType.STRING)
    private EmployeeStatus employeeStatus;

    @Column(name = "role", nullable = false)
    @NotNull(message = "Role cannot be blank")
    private Role role = Role.EMPLOYEE;

    @OneToMany(mappedBy = "approvedBy")
    private final Set<Loan> approvedLoans = new LinkedHashSet<>();

    @OneToMany(mappedBy = "handledBy")
    private final Set<Transaction> handledTransactions = new LinkedHashSet<>();

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public EmployeeRole getEmployeeRole() {
        return employeeRole;
    }

    public void setEmployeeRole(EmployeeRole employeeRole) {
        this.employeeRole = employeeRole;
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
                .append("phone number : ", this.getPhoneNumber())
                .append("birth date : ", this.getDateOfBirth())
                .append("address : ", this.getAddress())
                .append("gender : ", this.getGender())
                .append("username : ", this.getUsername())
                .append("role : ", this.getEmployeeRole())
                .append("status : ", this.getEmployeeStatus())
                .toString();
    }
}
