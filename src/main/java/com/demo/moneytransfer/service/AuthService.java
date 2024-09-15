package com.demo.moneytransfer.service;

import com.demo.moneytransfer.model.Customer;
import com.demo.moneytransfer.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    public Customer register(Customer customer) {
        customer.setPassword(passwordEncoder.encode(customer.getPassword()));
        return customerRepository.save(customer);
    }

    public Customer login(String username, String password) {
        Customer customer = customerRepository.findByUsername(username);
        if (customer != null && passwordEncoder.matches(password, customer.getPassword())) {
            System.out.println("User Found");
            return customer;
        }
        System.out.println("User Not Found");
        return null;
    }
}