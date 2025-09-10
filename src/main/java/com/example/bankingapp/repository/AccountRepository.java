package com.example.bankingapp.repository;

import com.example.bankingapp.entities.account.Account;
import com.example.bankingapp.entities.account.AccountType;
import com.example.bankingapp.entities.customer.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long> {
    boolean existsByCustomerAndAccountType(Customer customer, AccountType type);
}
