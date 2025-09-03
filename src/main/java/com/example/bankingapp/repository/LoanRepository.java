package com.example.bankingapp.repository;

import com.example.bankingapp.entities.loan.Loan;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoanRepository extends JpaRepository<Loan, Long> {
}
