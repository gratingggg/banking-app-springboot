package com.example.bankingapp.specification;

import com.example.bankingapp.entities.customer.Customer;
import com.example.bankingapp.entities.notification.Notification;
import com.example.bankingapp.entities.notification.NotificationStatus;
import com.example.bankingapp.entities.notification.NotificationType;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalTime;

public class NotificationSpecifications {
    public static Specification<Notification> forCustomer(Customer customer){
        return (root, query, builder) ->
                builder.equal(root.get("customer"), customer);
    }

    public static Specification<Notification> withType(NotificationType type){
        return ((root, query, builder) ->
                (type == null)
                        ?builder.conjunction()
                        :builder.equal(root.get("notificationType"), type));
    }

    public static Specification<Notification> withStatus(NotificationStatus status){
        return ((root, query, builder) ->
                (status == null)
                        ?builder.conjunction()
                        :builder.equal(root.get("notificationStatus"), status));
    }

    public static Specification<Notification> withDate(LocalDate from, LocalDate to){
        return (root, query, builder) -> {
            if(from != null && to != null){
                return builder.between(root.get("date"), from.atStartOfDay(), to.atTime(LocalTime.MAX));
            }

            if(from != null){
                return builder.greaterThanOrEqualTo(root.get("date"), from.atStartOfDay());
            }

            if(to != null){
                return builder.lessThanOrEqualTo(root.get("date"), to.atTime(LocalTime.MAX));
            }

            return builder.conjunction();
        };
    }
}
