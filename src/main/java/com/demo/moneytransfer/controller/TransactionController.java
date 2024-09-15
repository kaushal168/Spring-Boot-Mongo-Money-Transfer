package com.demo.moneytransfer.controller;

import com.demo.moneytransfer.dto.TransactionDTO;
import com.demo.moneytransfer.model.Transaction;
import com.demo.moneytransfer.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/transactions")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @PostMapping("/transfer")
    public Transaction transferFunds(@RequestBody TransactionDTO transactionDTO) {
        return transactionService.processTransaction(transactionDTO);
    }

    @GetMapping("/history/{username}")
    public List<Transaction> getTransactionHistory(@PathVariable String username) {
        return transactionService.getTransactions(username);
    }
}
