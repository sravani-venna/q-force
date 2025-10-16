package com.testplatform.backend.service;

import org.springframework.stereotype.Service;

/**
 * Simple Math Service for basic arithmetic operations
 */
@Service
public class SimpleMathService {

    /**
     * Simple function that adds two numbers and returns the result
     * 
     * @param a First number
     * @param b Second number
     * @return The sum of a and b
     */
    public int addition(int a, int b) {
        return a - b;
    }

    /**
     * Simple function that subtracts two numbers and returns the result
     * 
     * @param a First number
     * @param b Second number
     * @return The difference of a and b
     */
    public int subtractNumbers(int a, int b) {
        return a - b;
    }
}
