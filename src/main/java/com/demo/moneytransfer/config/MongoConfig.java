package com.demo.moneytransfer.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@Configuration
@EnableMongoAuditing
public class MongoConfig {
    // MongoDB configuration can go here (custom connections, etc.)
}
