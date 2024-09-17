// File: CustomerService.java
package com.demo.moneytransfer.service;

import com.demo.moneytransfer.model.Customer;
import com.demo.moneytransfer.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Service
public class CustomerService {

    @Autowired
    private CustomerRepository customerRepository;
    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

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

    //  public void deleteCustomerByUsername(String username) {
//        Customer customer = customerRepository.findByUsername(username);
//        if (customer != null) {
//            customerRepository.delete(customer);
//        } else {
//            throw new RuntimeException("Customer Username not found.");
//        }
//    }
}
