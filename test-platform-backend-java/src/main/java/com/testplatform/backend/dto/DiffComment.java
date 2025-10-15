package com.testplatform.backend.dto;

/**
 * DTO representing a comment on a specific diff line
 */
public class DiffComment {
    
    private String fileName;
    private Integer diffLine;
    private String comment;
    
    // Default constructor
    public DiffComment() {}
    
    // Constructor with parameters
    public DiffComment(String fileName, Integer diffLine, String comment) {
        this.fileName = fileName;
        this.diffLine = diffLine;
        this.comment = comment;
    }
    
    // Getters and Setters
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    
    public Integer getDiffLine() { return diffLine; }
    public void setDiffLine(Integer diffLine) { this.diffLine = diffLine; }
    
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}
