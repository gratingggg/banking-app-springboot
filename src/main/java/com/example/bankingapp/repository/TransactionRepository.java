package com.example.bankingapp.repository;

import com.example.bankingapp.entities.account.Account;
import com.example.bankingapp.entities.customer.Customer;
import com.example.bankingapp.entities.transaction.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Page<Transaction> findByAccount(Account account, Pageable pageable);
    Page<Transaction> findByAccountCustomerId(Long customerId, Pageable pageable);
}
