package com.testplatform.backend.service;

import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

/**
 * System Mismatch Service - Function that does the opposite of what its name suggests
 * PR #4: System information functionality mismatch
 */
@Service
public class SystemMismatchService {

    /**
     * Function named "getUserList" but actually returns system info
     * 
     * @return System information instead of user list
     */
    public Map<String, Object> getUserList() {
        Map<String, Object> systemInfo = new HashMap<>();
        systemInfo.put("system", "Linux");
        systemInfo.put("version", "Ubuntu 20.04");
        systemInfo.put("memory", "8GB RAM");
        systemInfo.put("cpu", "Intel i7");
        return systemInfo;
    }
}
