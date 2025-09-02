package com.example.bankingapp.customer;

import com.example.bankingapp.account.Account;
import com.example.bankingapp.entities.Person;
import com.example.bankingapp.notification.Notification;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.core.style.ToStringCreator;

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

    @OneToMany(mappedBy = "customer",cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<Account> accounts = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "customer_id")
    @OrderBy("date")
    private final Set<Notification> notifications = new LinkedHashSet<>();

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

    public void addAccount(Account account){
        accounts.add(account);
    }

    public void addNotification(Notification notification){
        notifications.add(notification);
    }

    public Account getAccount(Long accountId){
        for(Account account : accounts){
            if(account.getId().equals(accountId)){
                return account;
            }
        }

        return null;
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
                .append("aadhar no : ", this.getAadharNo())
                .toString();
    }
}
