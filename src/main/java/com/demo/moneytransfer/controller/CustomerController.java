// File: CustomerController.java
package com.demo.moneytransfer.controller;

import com.demo.moneytransfer.dto.CustomerDTO;
import com.demo.moneytransfer.model.Customer;
import com.demo.moneytransfer.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/customers")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @GetMapping("/{id}")
    public Customer getCustomerById(@PathVariable String id) {
        return customerService.getCustomerById(id);
    }

    @PostMapping("/create")
    public Customer createCustomer(@RequestBody CustomerDTO customerDTO) {
        System.out.println("Account Balance received: " + customerDTO.getAccountBalance()); // Debug log
        Customer customer = new Customer();
        customer.setUsername((String) customerDTO.getUsername());
        customer.setPassword((String) customerDTO.getPassword());
        customer.setAccountBalance((double) customerDTO.getAccountBalance());
        return customerService.createCustomer(customer);
    }

    @PutMapping("/update")
    public Customer updateCustomer(@RequestBody Customer customer) {
        return customerService.updateCustomer(customer);
    }

    @DeleteMapping("/delete/{id}")
    public void deleteCustomer(@PathVariable String id) {
        customerService.deleteCustomer(id);
    }
}
