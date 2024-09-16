package com.demo.moneytransfer.service;

import com.demo.moneytransfer.dto.TransactionDTO;
import com.demo.moneytransfer.model.Customer;
import com.demo.moneytransfer.model.Transaction;
import com.demo.moneytransfer.repository.CustomerRepository;
import com.demo.moneytransfer.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;

@Service
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private CustomerRepository customerRepository;

    public Transaction processTransaction(TransactionDTO transactionDTO) {
        // Fetch sender and receiver details using username instead of ID
        System.out.println("In processTransaction");
        System.out.println("Sender: " + transactionDTO.getSenderUsername() + " Receiver: " + transactionDTO.getReceiverUsername());

        Customer sender = customerRepository.findByUsername(transactionDTO.getSenderUsername());
        Customer receiver = customerRepository.findByUsername(transactionDTO.getReceiverUsername());

        System.out.println(sender+"\n"+receiver);
        if (sender == null || receiver == null) {
            throw new RuntimeException("Sender or Receiver not found.");
        }

        // Ensure sender has sufficient balance
        if (sender.getAccountBalance() < transactionDTO.getAmount()) {
            throw new RuntimeException("Insufficient balance.");
        }

        // Deduct from sender and add to receiver
        sender.setAccountBalance(sender.getAccountBalance() - transactionDTO.getAmount());
        receiver.setAccountBalance(receiver.getAccountBalance() + transactionDTO.getAmount());

        // Save updated accounts
        customerRepository.save(sender);
        customerRepository.save(receiver);

        // Create and save the transaction
        Transaction transaction = new Transaction();
        transaction.setSenderUsername(transactionDTO.getSenderUsername());
        transaction.setReceiverUsername(transactionDTO.getReceiverUsername());
        transaction.setAmount(transactionDTO.getAmount());
        transaction.setTimestamp(new Date());

        return transactionRepository.save(transaction);
    }

    public List<Transaction> getTransactions(String username) {
        List<Transaction> sentTransactions = transactionRepository.findBySenderUsername(username);

        // Fetch transactions where the user is the receiver
        List<Transaction> receivedTransactions = transactionRepository.findByReceiverUsername(username);

        // Combine both lists into one
        List<Transaction> allTransactions = new ArrayList<>();
        allTransactions.addAll(sentTransactions);
        allTransactions.addAll(receivedTransactions);

        return allTransactions;
    }
}
