package com.reviewer.codereviewer.client.dto;

import java.util.List;
import java.util.ArrayList;

/**
 * Data Transfer Object for code issues from the backend API
 */
public class CodeIssue {
    
    public enum Type {
        ERROR, WARNING, SUGGESTION, INFO
    }
    
    private String message;
    private int line;
    private int column;
    private Type type;
    private String severity;
    private List<QuickFix> quickFixes;
    
    public CodeIssue() {
        this.quickFixes = new ArrayList<>();
    }
    
    public CodeIssue(String message, int line, int column, Type type) {
        this.message = message;
        this.line = line;
        this.column = column;
        this.type = type;
        this.quickFixes = new ArrayList<>();
    }
    
    public CodeIssue(String message, int line, Type type) {
        this(message, line, 0, type);
    }
    
    // Getters and Setters
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public int getLine() {
        return line;
    }
    
    public void setLine(int line) {
        this.line = line;
    }
    
    public int getColumn() {
        return column;
    }
    
    public void setColumn(int column) {
        this.column = column;
    }
    
    public Type getType() {
        return type;
    }
    
    public void setType(Type type) {
        this.type = type;
    }
    
    public String getSeverity() {
        return severity;
    }
    
    public void setSeverity(String severity) {
        this.severity = severity;
    }
    
    public List<QuickFix> getQuickFixes() {
        return quickFixes;
    }
    
    public void setQuickFixes(List<QuickFix> quickFixes) {
        this.quickFixes = quickFixes != null ? quickFixes : new ArrayList<>();
    }
    
    public void addQuickFix(QuickFix quickFix) {
        if (quickFix != null) {
            this.quickFixes.add(quickFix);
        }
    }
    
    public boolean hasQuickFixes() {
        return !quickFixes.isEmpty();
    }
    
    // For JavaFX TableView compatibility
    public int getLineNumber() {
        return getLine();
    }
    
    @Override
    public String toString() {
        return String.format("%s at line %d: %s", type, line, message);
    }
}
