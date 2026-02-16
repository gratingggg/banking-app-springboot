package com.example.bankingapp.specification;

import com.example.bankingapp.entities.loan.Loan;
import com.example.bankingapp.entities.loan.LoanStatus;
import com.example.bankingapp.entities.loan.LoanType;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalTime;

public class LoanSpecification {
    public static Specification<Loan> forCustomer(Long customerId){
        return ((root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("account").get("customer").get("id"), customerId));
    }

    public static Specification<Loan> withStatus(LoanStatus status){
        return ((root, query, criteriaBuilder) ->
                (status == null)
                        ?criteriaBuilder.conjunction()
                        :criteriaBuilder.equal(root.get("loanStatus"), status));
    }

    public static Specification<Loan> withType(LoanType type){
        return ((root, query, criteriaBuilder) ->
                (type == null)
                        ?criteriaBuilder.conjunction()
                :criteriaBuilder.equal(root.get("loanType"), type));
    }

    public static Specification<Loan> dateBetween(LocalDate from, LocalDate to){
        return (root, query, builder) -> {
            if(from != null && to != null){
                return builder.between(root.get("dateOfIssuance"), from.atStartOfDay(), to.atTime(LocalTime.MAX));
            }
            else if(from != null){
                return builder.greaterThanOrEqualTo(root.get("dateOfIssuance"), from.atStartOfDay());
            }
            else if(to != null){
                return builder.lessThanOrEqualTo(root.get("dateOfIssuance"), to.atTime(LocalTime.MAX));
            }
            else return builder.conjunction();
        };
    }
}
