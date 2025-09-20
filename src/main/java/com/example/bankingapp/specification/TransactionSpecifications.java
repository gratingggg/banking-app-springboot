package com.example.bankingapp.specification;

import com.example.bankingapp.entities.account.Account;
import com.example.bankingapp.entities.transaction.Transaction;
import com.example.bankingapp.entities.transaction.TransactionStatus;
import com.example.bankingapp.entities.transaction.TransactionType;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalTime;

public class TransactionSpecifications {
    public static Specification<Transaction> forCustomers(Long customerId){
        return (root, query, builder) -> {
            assert query != null;
            query.distinct(true);
            var fromJoin = root.join("fromAccount", JoinType.LEFT);
            var toJoin = root.join("toAccount", JoinType.LEFT);
            Predicate fromCustomer = builder.equal(fromJoin.get("customer").get("id"), customerId);
            Predicate toCustomer = builder.equal(toJoin.get("customer").get("id"), customerId);
            return builder.or(fromCustomer, toCustomer);
        };
    }

    public static Specification<Transaction> forAccounts(Account account){
        return (root, query, builder) ->
                builder.or(builder.equal(root.get("fromAccount"), account),
                        builder.equal(root.get("toAccount"), account));
    }

    public static Specification<Transaction> forFromAccounts(Account fromAccount){
        return (root, query, builder)-> builder.equal(root.get("fromAccount"), fromAccount);
    }

    public static Specification<Transaction> forToAccounts(Account toAccount){
        return (root, query, builder)-> builder.equal(root.get("toAccount"), toAccount);
    }

    public static Specification<Transaction> withStatus(TransactionStatus status){
        return (status == null) ? null :
                (root, query, builder) ->
                        builder.equal(root.get("transactionStatus"), status);
    }

    public static Specification<Transaction> withType(TransactionType type){
        return (type == null) ? null :
                (root, query, builder) ->
                        builder.equal(root.get("transactionType"), type);
    }

    public static Specification<Transaction> dateBetween(LocalDate from, LocalDate to){
        return (root, query, builder) -> {
            if(from != null && to != null){
                return builder.between(root.get("dateOfTransaction"), from.atStartOfDay() , to.atTime(LocalTime.MAX));
            }
            if(from != null){
                return builder.greaterThanOrEqualTo(root.get("dateOfTransaction"), from.atStartOfDay());
            }
            if(to != null){
                return builder.lessThanOrEqualTo(root.get("dateOfTransaction"), to.atTime(LocalTime.MAX));
            }
            else return null;
        };
    }
}
