package com.example.bankingapp.repository;

import com.example.bankingapp.entities.transaction.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
}
