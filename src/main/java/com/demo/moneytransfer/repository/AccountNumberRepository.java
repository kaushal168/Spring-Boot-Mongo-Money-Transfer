package com.demo.moneytransfer.repository;

import com.demo.moneytransfer.model.AccountNumber;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface AccountNumberRepository extends MongoRepository<AccountNumber, String> {

    // Find the first unassigned account number
    Optional<AccountNumber> findFirstByAssignedFalse();
}