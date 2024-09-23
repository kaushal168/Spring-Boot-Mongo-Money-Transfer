// File: AccountNumberInitializer.java
package com.demo.moneytransfer.config;

import com.demo.moneytransfer.model.AccountNumber;
import com.demo.moneytransfer.repository.AccountNumberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Configuration
public class AccountNumberInitializer {

    @Autowired
    private AccountNumberRepository accountNumberRepository;

    @PostConstruct
    public void init() {
        if (accountNumberRepository.count() == 0) {
            List<AccountNumber> accountNumbers = IntStream.range(100001, 109999) // Example account numbers
                    .mapToObj(num -> new AccountNumber(String.valueOf(num), false))
                    .collect(Collectors.toList());

            accountNumberRepository.saveAll(accountNumbers);
        }
    }
}