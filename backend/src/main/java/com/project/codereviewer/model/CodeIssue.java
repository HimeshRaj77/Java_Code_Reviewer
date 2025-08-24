package com.project.codereviewer.model;

import java.util.ArrayList;
import java.util.List;

public class CodeIssue {
    public enum Type { ERROR, SUGGESTION }
    private int lineNumber;
    private String message;
    private Type type;
    private String severity; // e.g., "warning", "critical"
    private String filePath;
    private List<QuickFix> quickFixes;
    private String sourceCode;
    private String lineContent;

    public CodeIssue(int lineNumber, String message, Type type, String severity) {
        this.lineNumber = lineNumber;
        this.message = message;
        this.type = type;
        this.severity = severity;
        this.quickFixes = new ArrayList<>();
    }

    public int getLineNumber() { return lineNumber; }
    public String getMessage() { return message; }
    public Type getType() { return type; }
    public String getSeverity() { return severity; }
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    public List<QuickFix> getQuickFixes() { return quickFixes; }
    public void addQuickFix(QuickFix fix) { this.quickFixes.add(fix); }

    public String getSourceCode() { return sourceCode; }
    public void setSourceCode(String sourceCode) { this.sourceCode = sourceCode; }
    public String getLineContent() { return lineContent; }
    public void setLineContent(String lineContent) { this.lineContent = lineContent; }
    
    public boolean hasQuickFixes() {
        return quickFixes != null && !quickFixes.isEmpty();
    }

    public List<QuickFix> getAvailableFixes() {
        return quickFixes;
    }
}