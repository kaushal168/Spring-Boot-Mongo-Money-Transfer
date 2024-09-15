package com.demo.moneytransfer.repository;

import com.demo.moneytransfer.model.Transaction;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface TransactionRepository extends MongoRepository<Transaction, String> {
    List<Transaction> findBySenderUsername(String senderUsername);
}
