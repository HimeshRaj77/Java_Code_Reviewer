package com.reviewer.codereviewer.client.dto;

/**
 * Data Transfer Object for quick fixes from the backend API
 */
public class QuickFix {
    private String description;
    private String action;
    private String replacement;
    private int startLine;
    private int endLine;
    
    public QuickFix() {
    }
    
    public QuickFix(String description, String action) {
        this.description = description;
        this.action = action;
    }
    
    public QuickFix(String description, String action, String replacement, int startLine, int endLine) {
        this.description = description;
        this.action = action;
        this.replacement = replacement;
        this.startLine = startLine;
        this.endLine = endLine;
    }
    
    // Getters and Setters
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getAction() {
        return action;
    }
    
    public void setAction(String action) {
        this.action = action;
    }
    
    public String getReplacement() {
        return replacement;
    }
    
    public void setReplacement(String replacement) {
        this.replacement = replacement;
    }
    
    public int getStartLine() {
        return startLine;
    }
    
    public void setStartLine(int startLine) {
        this.startLine = startLine;
    }
    
    public int getEndLine() {
        return endLine;
    }
    
    public void setEndLine(int endLine) {
        this.endLine = endLine;
    }
    
    @Override
    public String toString() {
        return description != null ? description : action;
    }
}
