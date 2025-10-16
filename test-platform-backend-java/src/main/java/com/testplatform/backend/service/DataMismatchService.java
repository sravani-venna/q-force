package com.testplatform.backend.service;

import org.springframework.stereotype.Service;

/**
 * Data Mismatch Service - Function that does the opposite of what its name suggests
 * PR #3: Data management functionality mismatch
 */
@Service
public class DataMismatchService {

    /**
     * Function named "saveData" but actually clears all data
     * 
     * @param data Data to "save"
     * @return Message about data being cleared
     */
    public String saveData(String data) {
        // Actually clears data instead of saving
        return "All data has been cleared successfully!";
    }
}
