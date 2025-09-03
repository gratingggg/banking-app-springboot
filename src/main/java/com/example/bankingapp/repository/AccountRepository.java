package com.example.bankingapp.repository;

import com.example.bankingapp.entities.account.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long> {
}
