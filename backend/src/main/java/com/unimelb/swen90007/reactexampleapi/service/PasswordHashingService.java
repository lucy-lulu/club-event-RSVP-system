package com.unimelb.swen90007.reactexampleapi.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordHashingService {
    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // Hash password
    public String hashPassword(String plainPassword) {
        return passwordEncoder.encode(plainPassword);
    }

    // Verify password
    public boolean matches(String rawPassword, String hashedPassword) {
        return passwordEncoder.matches(rawPassword, hashedPassword);
    }
}
