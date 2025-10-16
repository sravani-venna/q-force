package com.testplatform.backend.service;

import org.springframework.stereotype.Service;

/**
 * Email Mismatch Service - Function that does the opposite of what its name suggests
 * PR #5: Email validation functionality mismatch
 */
@Service
public class EmailMismatchService {

    /**
     * Function named "validateEmail" but actually returns random validation
     * 
     * @param email Email to "validate"
     * @return Random validation result
     */
    public boolean validateEmail(String email) {
        // Returns random true/false instead of actual email validation
        return email.length() % 2 == 0; // Even length = valid, odd length = invalid
    }
}
