package com.reviewer.codereviewer.client.dto;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Code Issue DTO Tests")
class CodeIssueTest {
    
    private CodeIssue issue;
    
    @BeforeEach
    void setUp() {
        issue = new CodeIssue();
    }
    
    @Test
    @DisplayName("Should initialize with empty quick fixes list")
    void shouldInitializeWithEmptyQuickFixesList() {
        assertNotNull(issue.getQuickFixes());
        assertTrue(issue.getQuickFixes().isEmpty());
        assertFalse(issue.hasQuickFixes());
    }
    
    @Test
    @DisplayName("Should initialize with provided parameters")
    void shouldInitializeWithProvidedParameters() {
        String message = "Test error message";
        int line = 42;
        int column = 10;
        CodeIssue.Type type = CodeIssue.Type.ERROR;
        
        CodeIssue issue = new CodeIssue(message, line, column, type);
        
        assertEquals(message, issue.getMessage());
        assertEquals(line, issue.getLine());
        assertEquals(column, issue.getColumn());
        assertEquals(type, issue.getType());
        assertNotNull(issue.getQuickFixes());
        assertTrue(issue.getQuickFixes().isEmpty());
    }
    
    @Test
    @DisplayName("Should initialize with line only constructor")
    void shouldInitializeWithLineOnlyConstructor() {
        String message = "Test warning";
        int line = 25;
        CodeIssue.Type type = CodeIssue.Type.WARNING;
        
        CodeIssue issue = new CodeIssue(message, line, type);
        
        assertEquals(message, issue.getMessage());
        assertEquals(line, issue.getLine());
        assertEquals(0, issue.getColumn()); // Default column should be 0
        assertEquals(type, issue.getType());
    }
    
    @Test
    @DisplayName("Should set and get message")
    void shouldSetAndGetMessage() {
        String message = "This is a test message";
        issue.setMessage(message);
        assertEquals(message, issue.getMessage());
    }
    
    @Test
    @DisplayName("Should set and get line number")
    void shouldSetAndGetLineNumber() {
        int line = 123;
        issue.setLine(line);
        assertEquals(line, issue.getLine());
    }
    
    @Test
    @DisplayName("Should set and get column number")
    void shouldSetAndGetColumnNumber() {
        int column = 45;
        issue.setColumn(column);
        assertEquals(column, issue.getColumn());
    }
    
    @Test
    @DisplayName("Should set and get type")
    void shouldSetAndGetType() {
        CodeIssue.Type type = CodeIssue.Type.SUGGESTION;
        issue.setType(type);
        assertEquals(type, issue.getType());
    }
    
    @Test
    @DisplayName("Should set and get severity")
    void shouldSetAndGetSeverity() {
        String severity = "HIGH";
        issue.setSeverity(severity);
        assertEquals(severity, issue.getSeverity());
    }
    
    @Test
    @DisplayName("Should add quick fix")
    void shouldAddQuickFix() {
        QuickFix fix = new QuickFix("Fix description", "Fix action");
        
        issue.addQuickFix(fix);
        
        assertEquals(1, issue.getQuickFixes().size());
        assertEquals(fix, issue.getQuickFixes().get(0));
        assertTrue(issue.hasQuickFixes());
    }
    
    @Test
    @DisplayName("Should handle null quick fix gracefully")
    void shouldHandleNullQuickFixGracefully() {
        issue.addQuickFix(null);
        
        assertTrue(issue.getQuickFixes().isEmpty());
        assertFalse(issue.hasQuickFixes());
    }
    
    @Test
    @DisplayName("Should set quick fixes list")
    void shouldSetQuickFixesList() {
        QuickFix fix1 = new QuickFix("Fix 1", "Action 1");
        QuickFix fix2 = new QuickFix("Fix 2", "Action 2");
        
        java.util.List<QuickFix> fixes = java.util.Arrays.asList(fix1, fix2);
        issue.setQuickFixes(fixes);
        
        assertEquals(2, issue.getQuickFixes().size());
        assertTrue(issue.hasQuickFixes());
        assertEquals(fix1, issue.getQuickFixes().get(0));
        assertEquals(fix2, issue.getQuickFixes().get(1));
    }
    
    @Test
    @DisplayName("Should handle null quick fixes list gracefully")
    void shouldHandleNullQuickFixesListGracefully() {
        issue.setQuickFixes(null);
        
        assertNotNull(issue.getQuickFixes());
        assertTrue(issue.getQuickFixes().isEmpty());
        assertFalse(issue.hasQuickFixes());
    }
    
    @Test
    @DisplayName("Should test all enum types")
    void shouldTestAllEnumTypes() {
        // Test that all enum types are available
        assertEquals(4, CodeIssue.Type.values().length);
        assertEquals(CodeIssue.Type.ERROR, CodeIssue.Type.valueOf("ERROR"));
        assertEquals(CodeIssue.Type.WARNING, CodeIssue.Type.valueOf("WARNING"));
        assertEquals(CodeIssue.Type.SUGGESTION, CodeIssue.Type.valueOf("SUGGESTION"));
        assertEquals(CodeIssue.Type.INFO, CodeIssue.Type.valueOf("INFO"));
    }
    
    @Test
    @DisplayName("Should produce meaningful toString")
    void shouldProduceMeaningfulToString() {
        issue.setType(CodeIssue.Type.ERROR);
        issue.setLine(42);
        issue.setMessage("Test error message");
        
        String result = issue.toString();
        
        assertTrue(result.contains("ERROR"));
        assertTrue(result.contains("42"));
        assertTrue(result.contains("Test error message"));
    }
}
