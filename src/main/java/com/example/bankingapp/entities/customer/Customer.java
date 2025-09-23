package com.example.bankingapp.entities.customer;

import com.example.bankingapp.Role;
import com.example.bankingapp.entities.account.Account;
import com.example.bankingapp.entities.account.AccountStatus;
import com.example.bankingapp.entities.baseentities.Person;
import com.example.bankingapp.entities.loan.Loan;
import com.example.bankingapp.entities.loan.LoanStatus;
import com.example.bankingapp.entities.notification.Notification;
import com.example.bankingapp.exception.AccountBalanceNotZeroException;
import com.example.bankingapp.exception.NonClosedLoanException;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.springframework.core.style.ToStringCreator;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "customers")
public class Customer extends Person {
    @Column(name = "aadhar_no")
    @NotBlank(message = "Aadhar no cannot be blank")
    @Pattern(regexp = "^\\d{12}$")
    private String aadharNo;

    @OneToMany(mappedBy = "customer")
    private final List<Account> accounts = new ArrayList<>();

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    private final Set<Notification> notifications = new LinkedHashSet<>();

    @Column(name = "role", nullable = false)
    @NotNull(message = "Role cannot be blank")
    private Role role = Role.CUSTOMER;

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getAadharNo() {
        return aadharNo;
    }

    public void setAadharNo(String aadharNo) {
        this.aadharNo = aadharNo;
    }

    public List<Account> getAccounts() {
        return accounts;
    }

    public Set<Notification> getNotifications() {
        return notifications;
    }

    public void addAccount(Account account) {
        accounts.add(account);
        account.setCustomer(this);
    }

    public void addNotification(Notification notification) {
        notifications.add(notification);
        notification.setCustomer(this);
    }

    public Account getAccount(Long accountId) {
        for (Account account : accounts) {
            if (account.getId().equals(accountId)) {
                return account;
            }
        }
        return null;
    }

    public synchronized boolean removeAccount(Account account) {
        for(Loan loan : account.getLoans()){
            if(!loan.getLoanStatus().equals(LoanStatus.CLOSED)){
                throw new NonClosedLoanException();
            }
        }
        if (account.getBalance().compareTo(BigDecimal.ZERO) == 0) {
            account.setAccountStatus(AccountStatus.CLOSED);
            return true;
        }
        throw new AccountBalanceNotZeroException();
    }

    @Override
    public String toString() {
        return new ToStringCreator(this)
                .append("id : ", this.getId())
                .append("name : ", this.getName())
                .append("phone number : ", this.getPhoneNumber())
                .append("birth date : ", this.getDateOfBirth())
                .append("address : ", this.getAddress())
                .append("gender : ", this.getGender())
                .append("aadhar no : ", this.getAadharNo())
                .toString();
    }
}
