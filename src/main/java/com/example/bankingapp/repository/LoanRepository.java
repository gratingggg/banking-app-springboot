package com.example.bankingapp.repository;

import com.example.bankingapp.entities.loan.Loan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface LoanRepository extends JpaRepository<Loan, Long>, JpaSpecificationExecutor<Loan> {
    @Query("""
       SELECT l
       FROM Loan l
       LEFT JOIN FETCH l.transactions
       WHERE l.id = :id
       """)
    Optional<Loan> findByIdWithTransactions(Long id);

}
