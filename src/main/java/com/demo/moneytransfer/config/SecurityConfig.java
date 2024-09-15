package com.demo.moneytransfer.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf().disable()  // Disable CSRF for development; enable it for production
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/auth/**", "/customers/**").permitAll()  // Allow access to auth and customer APIs
                        .anyRequest().authenticated()
                )
                .httpBasic();  // Enable Basic Authentication (can be replaced with JWT)

        return http.build();
    }
}