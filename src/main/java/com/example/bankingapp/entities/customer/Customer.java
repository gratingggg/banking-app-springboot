package com.example.bankingapp.entities.customer;

import com.example.bankingapp.Role;
import com.example.bankingapp.entities.account.Account;
import com.example.bankingapp.entities.account.AccountStatus;
import com.example.bankingapp.entities.baseentities.Person;
import com.example.bankingapp.entities.notification.Notification;
import com.example.bankingapp.exception.AccountBalanceNotZeroException;
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

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "customer_id")
    @OrderBy("date")
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
        if (account.getBalance().compareTo(BigDecimal.ZERO) == 0) {
            accounts.remove(account);
            account.setCustomer(null);
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
