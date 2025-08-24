package com.project.codereviewer.model;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashMap;

public class AnalysisRecord {
    private LocalDateTime timestamp;
    private String fileName;
    private Map<String, Integer> complexityMetrics;
    private int totalIssues;
    private int totalErrors;
    private int totalSuggestions;
    
    public AnalysisRecord(String fileName) {
        this.timestamp = LocalDateTime.now();
        this.fileName = fileName;
        this.complexityMetrics = new HashMap<>();
        this.totalIssues = 0;
        this.totalErrors = 0;
        this.totalSuggestions = 0;
    }
    
    // Getters and setters
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    
    public Map<String, Integer> getComplexityMetrics() { return complexityMetrics; }
    public void setComplexityMetrics(Map<String, Integer> complexityMetrics) { this.complexityMetrics = complexityMetrics; }
    
    public int getTotalIssues() { return totalIssues; }
    public void setTotalIssues(int totalIssues) { this.totalIssues = totalIssues; }
    
    public int getTotalErrors() { return totalErrors; }
    public void setTotalErrors(int totalErrors) { this.totalErrors = totalErrors; }
    
    public int getTotalSuggestions() { return totalSuggestions; }
    public void setTotalSuggestions(int totalSuggestions) { this.totalSuggestions = totalSuggestions; }
    
    public void addComplexityMetric(String methodName, int complexity) {
        complexityMetrics.put(methodName, complexity);
    }
    
    @Override
    public String toString() {
        return String.format("AnalysisRecord{timestamp=%s, fileName='%s', totalIssues=%d, totalErrors=%d, totalSuggestions=%d, complexityMetrics=%s}",
                timestamp, fileName, totalIssues, totalErrors, totalSuggestions, complexityMetrics);
    }
} 