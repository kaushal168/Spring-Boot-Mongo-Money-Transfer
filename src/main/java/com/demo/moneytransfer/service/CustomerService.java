// File: CustomerService.java
package com.demo.moneytransfer.service;

import com.demo.moneytransfer.model.Account;
import com.demo.moneytransfer.model.AccountNumber;
import com.demo.moneytransfer.model.Customer;
import com.demo.moneytransfer.repository.AccountNumberRepository;
import com.demo.moneytransfer.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CustomerService {

    @Autowired
    private CustomerRepository customerRepository;
    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Autowired
    private AccountNumberRepository accountNumberRepository;

    public Customer getCustomerByUsername(String username) {
        return customerRepository.findByUsername(username);
    }

    public Customer createCustomer(Customer customer) {
        return customerRepository.save(customer);
    }

    // Update password for a customer by username after validating old password
    public Customer updatePassword(String username, String oldPassword, String newPassword) {
        Customer customer = customerRepository.findByUsername(username);
        if (customer == null) {
            throw new RuntimeException("Customer not found with username: " + username);
        }

        // Validate old password
        if (!passwordEncoder.matches(oldPassword, customer.getPassword())) {
            throw new RuntimeException("Old password does not match.");
        }

        // Hash and update the new password
        customer.setPassword(passwordEncoder.encode(newPassword));
        return customerRepository.save(customer);
    }

    // Create a new account for a user
    public Customer createNewAccount(String username) {
        Customer customer = customerRepository.findByUsername(username);
        if (customer == null) {
            throw new RuntimeException("Customer not found");
        }

        // Find an available account number
        Optional<AccountNumber> optionalAccountNumber = accountNumberRepository.findFirstByAssignedFalse();
        if (optionalAccountNumber.isEmpty()) {
            throw new RuntimeException("No available account numbers.");
        }

        // Assign the account number
        AccountNumber accountNumber = optionalAccountNumber.get();
        accountNumber.setAssigned(true); // Mark as assigned
        accountNumberRepository.save(accountNumber); // Persist the change

        // Create a new account and assign an account number
        Account newAccount = new Account();
        newAccount.setAccountNumber(accountNumber.getAccountNumber());
        newAccount.setBalance(1000.0);

        // Add the new account to the customer's account list
        List<Account> accounts = customer.getAccounts();
        if(accounts == null) {
            accounts = new ArrayList<>();
        }
        accounts.add(newAccount);
        customer.setAccounts(accounts);
        return customerRepository.save(customer);
    }
}
