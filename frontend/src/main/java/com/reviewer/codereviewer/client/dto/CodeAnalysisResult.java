package com.reviewer.codereviewer.client.dto;

import java.util.List;
import java.util.ArrayList;

/**
 * Data Transfer Object for code analysis results from the backend API
 */
public class CodeAnalysisResult {
    private List<CodeIssue> errors;
    private List<CodeIssue> suggestions;
    private double score;
    private String analysisTime;
    
    public CodeAnalysisResult() {
        this.errors = new ArrayList<>();
        this.suggestions = new ArrayList<>();
        this.score = 0.0;
    }
    
    public CodeAnalysisResult(List<CodeIssue> errors, List<CodeIssue> suggestions, double score) {
        this.errors = errors != null ? errors : new ArrayList<>();
        this.suggestions = suggestions != null ? suggestions : new ArrayList<>();
        this.score = score;
    }
    
    // Getters and Setters
    public List<CodeIssue> getErrors() {
        return errors;
    }
    
    public void setErrors(List<CodeIssue> errors) {
        this.errors = errors;
    }
    
    public List<CodeIssue> getSuggestions() {
        return suggestions;
    }
    
    public void setSuggestions(List<CodeIssue> suggestions) {
        this.suggestions = suggestions;
    }
    
    public double getScore() {
        return score;
    }
    
    public void setScore(double score) {
        this.score = score;
    }
    
    public String getAnalysisTime() {
        return analysisTime;
    }
    
    public void setAnalysisTime(String analysisTime) {
        this.analysisTime = analysisTime;
    }
    
    public boolean hasIssues() {
        return !errors.isEmpty() || !suggestions.isEmpty();
    }
    
    public int getTotalIssueCount() {
        return errors.size() + suggestions.size();
    }
}
