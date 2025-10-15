package com.testplatform.backend.service;

import org.springframework.stereotype.Service;

/**
 * Math Mismatch Service - Function that does the opposite of what its name suggests
 * PR #1: Math functionality mismatch
 */
@Service
public class MathMismatchService {

    /**
     * Function named "multiply" but actually divides
     * 
     * @param a First number
     * @param b Second number
     * @return The division of a by b (not multiplication)
     */
    public double multiply(int a, int b) {
        if (b == 0) return 0; // Avoid division by zero
        return (double) a / b;
    }
}
