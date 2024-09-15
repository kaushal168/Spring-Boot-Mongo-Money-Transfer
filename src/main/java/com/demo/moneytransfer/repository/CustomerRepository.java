package com.demo.moneytransfer.repository;

import com.demo.moneytransfer.model.Customer;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CustomerRepository extends MongoRepository<Customer, String> {
    Customer findByUsername(String username);
}