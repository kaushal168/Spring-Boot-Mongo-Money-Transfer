// File: AuthController.java
package com.demo.moneytransfer.controller;

import com.demo.moneytransfer.dto.CustomerDTO;
import com.demo.moneytransfer.model.Customer;
import com.demo.moneytransfer.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public Customer register(@RequestBody CustomerDTO customerDTO) {
        System.out.println("Account Balance received: " + customerDTO.getAccountBalance()); // Debug log
        Customer customer = new Customer();
        customer.setUsername((String) customerDTO.getUsername());
        customer.setPassword((String) customerDTO.getPassword());
        customer.setAccountBalance((double) customerDTO.getAccountBalance());
        return authService.register(customer);
    }

    @PostMapping("/login")
    public Customer login(@RequestParam String username, @RequestParam String password) {
        return authService.login(username, password);
    }
}
