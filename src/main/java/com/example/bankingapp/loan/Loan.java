package com.example.bankingapp.loan;

import com.example.bankingapp.entities.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

enum LoanType{
    HOME,
    PERSONAL,
    VEHICLE,
    EDUCATION,
    BUSiNESS,
    GOLD
}

enum Status{
    PENDING,
    APPROVED,
    REJECTED,
    DISBURSED,
    CLOSED,
    DEFAULTED
}

@Entity
@Table(name = "loans")
public class Loan extends BaseEntity {

}
