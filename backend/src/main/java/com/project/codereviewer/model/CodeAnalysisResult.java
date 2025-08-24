package com.project.codereviewer.model;

import java.util.ArrayList;
import java.util.List;

public class CodeAnalysisResult {
    private List<CodeIssue> errors = new ArrayList<>();
    private List<CodeIssue> suggestions = new ArrayList<>();

    public List<CodeIssue> getErrors() { return errors; }
    public void setErrors(List<CodeIssue> errors) { this.errors = errors; }
    public List<CodeIssue> getSuggestions() { return suggestions; }
    public void setSuggestions(List<CodeIssue> suggestions) { this.suggestions = suggestions; }
} 