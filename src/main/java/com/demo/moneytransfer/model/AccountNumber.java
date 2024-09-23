package com.demo.moneytransfer.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "account_numbers")
public class AccountNumber {

    @Id
    private String id;
    private String accountNumber;
    private boolean assigned;

    // Constructors, Getters, and Setters
    public AccountNumber() {}

    public AccountNumber(String accountNumber, boolean assigned) {
        this.accountNumber = accountNumber;
        this.assigned = assigned;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public boolean isAssigned() {
        return assigned;
    }

    public void setAssigned(boolean assigned) {
        this.assigned = assigned;
    }
}