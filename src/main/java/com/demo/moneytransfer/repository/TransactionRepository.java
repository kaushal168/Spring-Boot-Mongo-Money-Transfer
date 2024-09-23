package com.demo.moneytransfer.repository;

import com.demo.moneytransfer.model.Transaction;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends MongoRepository<Transaction, String> {
    List<Transaction> findBySenderUsername(String senderUsername);
    List<Transaction> findByReceiverUsername(String receiverUsername);
}
