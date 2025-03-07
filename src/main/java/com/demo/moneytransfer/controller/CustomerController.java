// File: CustomerController.java
package com.demo.moneytransfer.controller;

import com.demo.moneytransfer.dto.CustomerDTO;
import com.demo.moneytransfer.model.Customer;
import com.demo.moneytransfer.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/customers")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @GetMapping("/username/{username}")
    public Customer getCustomerByUsername(@PathVariable String username) {
        return customerService.getCustomerByUsername(username);
    }

    @PostMapping("/create-account")
    public Customer createNewAccount(@RequestParam String username) {
        return customerService.createNewAccount(username);
    }

    @PutMapping("/update-password")
    public ResponseEntity<String> updatePassword(
            @RequestParam String username,
            @RequestParam String oldPassword,
            @RequestParam String newPassword) {

        try {
            customerService.updatePassword(username, oldPassword, newPassword);
            return new ResponseEntity<>("Password updated successfully!", HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}
