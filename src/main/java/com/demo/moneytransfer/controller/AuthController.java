// File: AuthController.java
package com.demo.moneytransfer.controller;

import com.demo.moneytransfer.dto.CustomerDTO;
import com.demo.moneytransfer.model.Customer;
import com.demo.moneytransfer.service.AuthService;
import com.demo.moneytransfer.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private CustomerService customerService;

    @PostMapping("/register")
    public Customer register(@RequestBody CustomerDTO customerDTO) {
        Customer customer = new Customer();
        customer.setUsername((String) customerDTO.getUsername());
        customer.setPassword((String) customerDTO.getPassword());
        customer.setName((String) customerDTO.getName());
        customer.setEmail((String) customerDTO.getEmail());

        // Call the service to register the user
        Customer registeredCustomer = authService.register(customer);

        // Create a default account for the customer after registration
        registeredCustomer = customerService.createNewAccount(registeredCustomer.getUsername());
        return registeredCustomer;
    }

    @PostMapping("/login")
    public Customer login(@RequestParam String username, @RequestParam String password) {
        return authService.login(username, password);
    }
}
