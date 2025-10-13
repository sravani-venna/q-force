package com.testplatform.backend.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Pattern;

public class ModelUtils {
    
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    
    /**
     * Generate unique ID
     */
    public static String generateId() {
        return System.currentTimeMillis() + "-" + 
               Integer.toHexString((int) (Math.random() * 0x1000000));
    }
    
    /**
     * Validate email format
     */
    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }
    
    /**
     * Validate password strength
     */
    public static PasswordValidationResult validatePassword(String password) {
        if (password == null) {
            return new PasswordValidationResult(false, List.of("Password cannot be null"), 0);
        }
        
        List<String> errors = new java.util.ArrayList<>();
        
        if (password.length() < 8) {
            errors.add("Password must be at least 8 characters long");
        }
        
        if (!password.matches(".*[A-Z].*")) {
            errors.add("Password must contain at least one uppercase letter");
        }
        
        if (!password.matches(".*[a-z].*")) {
            errors.add("Password must contain at least one lowercase letter");
        }
        
        if (!password.matches(".*\\d.*")) {
            errors.add("Password must contain at least one number");
        }
        
        if (!password.matches(".*[!@#$%^&*(),.?\":{}|<>].*")) {
            errors.add("Password must contain at least one special character");
        }
        
        int strength = calculatePasswordStrength(password);
        
        return new PasswordValidationResult(errors.isEmpty(), errors, strength);
    }
    
    /**
     * Calculate password strength score (0-100)
     */
    public static int calculatePasswordStrength(String password) {
        if (password == null) return 0;
        
        int score = 0;
        
        // Length bonus
        score += Math.min(password.length() * 4, 25);
        
        // Character variety bonus
        if (password.matches(".*[A-Z].*")) score += 15;
        if (password.matches(".*[a-z].*")) score += 15;
        if (password.matches(".*\\d.*")) score += 15;
        if (password.matches(".*[!@#$%^&*(),.?\":{}|<>].*")) score += 15;
        
        // Complexity bonus
        if (password.length() >= 12) score += 10;
        if (password.replaceAll("[^A-Z]", "").length() >= 2) score += 5;
        if (password.replaceAll("[^\\d]", "").length() >= 2) score += 5;
        
        return Math.min(100, score);
    }
    
    /**
     * Format file size in human readable format
     */
    public static String formatFileSize(long bytes) {
        if (bytes == 0) return "0 Bytes";
        
        int k = 1024;
        String[] sizes = {"Bytes", "KB", "MB", "GB", "TB"};
        int i = (int) Math.floor(Math.log(bytes) / Math.log(k));
        
        return String.format("%.2f %s", bytes / Math.pow(k, i), sizes[i]);
    }
    
    /**
     * Format duration in human readable format
     */
    public static String formatDuration(long milliseconds) {
        if (milliseconds < 1000) {
            return milliseconds + "ms";
        } else if (milliseconds < 60000) {
            return String.format("%.1fs", milliseconds / 1000.0);
        } else if (milliseconds < 3600000) {
            return String.format("%.1fm", milliseconds / 60000.0);
        } else {
            return String.format("%.1fh", milliseconds / 3600000.0);
        }
    }
    
    /**
     * Format LocalDateTime to ISO string
     */
    public static String formatDateTime(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null;
    }
    
    // Inner class for password validation result
    public static class PasswordValidationResult {
        private final boolean isValid;
        private final List<String> errors;
        private final int strength;
        
        public PasswordValidationResult(boolean isValid, List<String> errors, int strength) {
            this.isValid = isValid;
            this.errors = errors;
            this.strength = strength;
        }
        
        public boolean isValid() { return isValid; }
        public List<String> getErrors() { return errors; }
        public int getStrength() { return strength; }
    }
}
