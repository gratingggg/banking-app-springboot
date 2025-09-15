package com.example.bankingapp.repository;

import com.example.bankingapp.entities.account.Account;
import com.example.bankingapp.entities.customer.Customer;
import com.example.bankingapp.entities.transaction.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    @Query("SELECT DISTINCT t FROM Transaction t LEFT JOIN t.fromAccount fa LEFT JOIN t.toAccount ta WHERE fa.customer = :customer OR ta.customer = :customer")
    Page<Transaction> findByCustomer(@Param("customer") Customer customer, Pageable pageable);

    @Query("SELECT DISTINCT t FROM Transaction t LEFT JOIN t.fromAccount fa LEFT JOIN t.toAccount ta WHERE fa = :account OR ta = :account")
    List<Transaction> findByAccount(@Param("account") Account account);

    @Query("SELECT DISTINCT t FROM Transaction t LEFT JOIN t.fromAccount fa LEFT JOIN t.toAccount ta WHERE fa = :account OR ta = :account")
    Page<Transaction> findByAccount(@Param("account") Account account, Pageable pageable);
}
