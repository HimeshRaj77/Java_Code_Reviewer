package com.reviewer.codereviewer.model;

public class CodeIssue {
    public enum Type { ERROR, SUGGESTION }
    private int lineNumber;
    private String message;
    private Type type;
    private String severity; // e.g., "warning", "critical"

    public CodeIssue(int lineNumber, String message, Type type, String severity) {
        this.lineNumber = lineNumber;
        this.message = message;
        this.type = type;
        this.severity = severity;
    }

    public int getLineNumber() { return lineNumber; }
    public String getMessage() { return message; }
    public Type getType() { return type; }
    public String getSeverity() { return severity; }
} 