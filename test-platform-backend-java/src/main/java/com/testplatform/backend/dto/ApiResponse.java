package com.testplatform.backend.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private boolean success;
    private T data;
    private Integer count;
    private String message;
    private ErrorDetails error;
    
    // Constructors
    public ApiResponse() {}
    
    public ApiResponse(boolean success) {
        this.success = success;
    }
    
    public ApiResponse(boolean success, T data) {
        this.success = success;
        this.data = data;
    }
    
    public ApiResponse(boolean success, T data, String message) {
        this.success = success;
        this.data = data;
        this.message = message;
    }
    
    // Static factory methods for success responses
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data);
    }
    
    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(true, data, message);
    }
    
    public static <T> ApiResponse<T> success(T data, Integer count) {
        ApiResponse<T> response = new ApiResponse<>(true, data);
        response.setCount(count);
        return response;
    }
    
    // Static factory methods for error responses
    public static <T> ApiResponse<T> error(String message) {
        ApiResponse<T> response = new ApiResponse<>(false);
        response.setError(new ErrorDetails(message));
        return response;
    }
    
    public static <T> ApiResponse<T> error(ErrorDetails error) {
        ApiResponse<T> response = new ApiResponse<>(false);
        response.setError(error);
        return response;
    }
    
    // Inner class for error details
    public static class ErrorDetails {
        private String message;
        private String code;
        private Object details;
        
        public ErrorDetails() {}
        
        public ErrorDetails(String message) {
            this.message = message;
        }
        
        public ErrorDetails(String message, String code) {
            this.message = message;
            this.code = code;
        }
        
        // Getters and Setters
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        
        public Object getDetails() { return details; }
        public void setDetails(Object details) { this.details = details; }
    }
    
    // Getters and Setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    
    public T getData() { return data; }
    public void setData(T data) { this.data = data; }
    
    public Integer getCount() { return count; }
    public void setCount(Integer count) { this.count = count; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public ErrorDetails getError() { return error; }
    public void setError(ErrorDetails error) { this.error = error; }
}
