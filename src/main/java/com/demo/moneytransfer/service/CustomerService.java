// File: CustomerService.java
package com.demo.moneytransfer.service;

import com.demo.moneytransfer.model.Customer;
import com.demo.moneytransfer.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    public Customer getCustomerByUsername(String username) {
        return customerRepository.findByUsername(username);
    }

    public Customer createCustomer(Customer customer) {
        return customerRepository.save(customer);
    }

//    public void deleteCustomerByUsername(String username) {
//        Customer customer = customerRepository.findByUsername(username);
//        if (customer != null) {
//            customerRepository.delete(customer);
//        } else {
//            throw new RuntimeException("Customer Username not found.");
//        }
//    }
}
