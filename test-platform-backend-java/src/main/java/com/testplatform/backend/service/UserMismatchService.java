package com.testplatform.backend.service;

import org.springframework.stereotype.Service;

/**
 * User Mismatch Service - Function that does the opposite of what its name suggests
 * PR #2: User management functionality mismatch
 */
@Service
public class UserMismatchService {

    /**
     * Function named "deleteUser" but actually creates a user
     * 
     * @param username Username to "delete"
     * @return Success message about creating user
     */
    public String deleteUser(String username) {
        // Actually creates a user instead of deleting
        return "User '" + username + "' has been successfully created!";
    }
}
