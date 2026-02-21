package com.example.bankingapp.utils;

public class Endpoints {
    private Endpoints(){}

    public static final String REGISTER = "/api/auth/register";
    public static final String LOGIN = "/api/auth/login";



    public static final String CUSTOMER_ME = "/api/customer/me";



    public static final String EMPLOYEE_ME = "/api/employee/me";
    public static final String EMPLOYEE_LOANS_PROCESS = "/api/employee/loans/{loanId}/process";
    public static final String EMPLOYEE_LOANS_DISBURSE = "/api/employee/loans/{loanId}/disburse";



    public static final String CUSTOMER_ACCOUNTS_ALL = "/api/customer/accounts/all";
    public static final String CUSTOMER_ACCOUNT_PARTICULAR = "/api/customer/accounts/{accountId}";
    public static final String CUSTOMER_ACCOUNT_TRANSACTION_ALL = "/api/customer/accounts/{accountId}/transactions";
    public static final String CUSTOMER_ACCOUNT_CREATE = "/api/customer/accounts";
    public static final String CUSTOMER_ACCOUNT_DELETE = "/api/customer/accounts/{accountId}/close";
    public static final String CUSTOMER_ACCOUNT_BALANCE = "/api/customer/accounts/{accountId}/balance";

    public static final String EMPLOYEE_ACCOUNTS_ALL = "/api/employee/customer/{customerId}/accounts";
    public static final String EMPLOYEE_ACCOUNT_PARTICULAR = "/api/employee/accounts/{accountId}";
    public static final String EMPLOYEE_ACCOUNT_TRANSACTIONS_ALL = "/api/employee/accounts/{accountId}/transactions";
    public static final String EMPLOYEE_ACCOUNT_CREATE = "/api/employee/customer/{customerId}/accounts";
    public static final String EMPLOYEE_ACCOUNT_DELETE = "/api/employee/accounts/{accountId}/close";
    public static final String EMPLOYEE_ACCOUNT_BALANCE = "/api/employee/accounts/{accountId}/balance";
    public static final String EMPLOYEE_ACCOUNT_DEPOSIT = "/api/employee/accounts/{accountId}/deposit";
    public static final String EMPLOYEE_ACCOUNT_WITHDRAWAL = "/api/employee/accounts/{accountId}/withdrawal";



    public static final String CUSTOMER_LOAN_APPLY = "/api/customer/loan/apply";
    public static final String CUSTOMER_LOAN_ALL = "/api/customer/loan";
    public static final String CUSTOMER_LOAN_REPAY = "/api/customer/loan/repay";
    public static final String CUSTOMER_LOAN_PARTICULAR = "/api/customer/loan/{loanId}";
    public static final String CUSTOMER_LOAN_TRANSACTIONS = "/api/customer/loan/{loanId}/transactions";

    public static final String EMPLOYEE_LOAN_APPLY = "/api/employee/customer/account/{accountId}/loan/apply";
    public static final String EMPLOYEE_CUSTOMER_LOAN_ALL = "/api/employee/customer/{customerId}/loan";
    public static final String EMPLOYEE_LOAN_PARTICULAR = "/api/employee/customer/loan/{loanId}";
    public static final String EMPLOYEE_LOAN_TRANSACTIONS = "/api/employee/customer/loan/{loanId}/transactions";
    public static final String EMPLOYEE_LOAN_ALL = "/api/employee/customer/loan/all";


    public static final String TRANSACTIONS_CUSTOMER_TRANSFER = "/api/customer/transactions/transfer";
    public static final String TRANSACTION_CUSTOMER = "/api/customer/transactions/{transactionId}";
    public static final String CUSTOMER_TRANSACTION_ALL = "/api/customer/transactions";

    public static final String TRANSACTIONS_EMPLOYEE_TRANSFER = "/api/employee/transactions/transfer";
    public static final String TRANSACTION_EMPLOYEE = "/api/employee/transactions/{transactionId}";
    public static final String EMPLOYEE_CUSTOMER_TRANSACTION_ALL = "/api/employee/customer/{customerId}/transactions";




    public static final String NOTIFICATIONS_ALL = "/api/notifications";
    public static final String NOTIFICATION_PARTICULAR = "/api/notifications/{notificationId}";
    public static final String NOTIFICATION_READ_ALL = "/api/notifications/read";
}
