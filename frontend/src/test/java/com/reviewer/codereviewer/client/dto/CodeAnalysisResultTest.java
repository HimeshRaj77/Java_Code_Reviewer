package com.reviewer.codereviewer.client.dto;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Code Analysis Result DTO Tests")
class CodeAnalysisResultTest {
    
    private CodeAnalysisResult result;
    
    @BeforeEach
    void setUp() {
        result = new CodeAnalysisResult();
    }
    
    @Test
    @DisplayName("Should initialize with empty lists")
    void shouldInitializeWithEmptyLists() {
        assertNotNull(result.getErrors());
        assertNotNull(result.getSuggestions());
        assertTrue(result.getErrors().isEmpty());
        assertTrue(result.getSuggestions().isEmpty());
        assertEquals(0.0, result.getScore());
    }
    
    @Test
    @DisplayName("Should initialize with provided lists")
    void shouldInitializeWithProvidedLists() {
        CodeIssue error = new CodeIssue("Error message", 1, CodeIssue.Type.ERROR);
        CodeIssue suggestion = new CodeIssue("Suggestion message", 2, CodeIssue.Type.SUGGESTION);
        
        List<CodeIssue> errors = Arrays.asList(error);
        List<CodeIssue> suggestions = Arrays.asList(suggestion);
        
        CodeAnalysisResult result = new CodeAnalysisResult(errors, suggestions, 85.5);
        
        assertEquals(1, result.getErrors().size());
        assertEquals(1, result.getSuggestions().size());
        assertEquals(85.5, result.getScore());
        assertEquals(error, result.getErrors().get(0));
        assertEquals(suggestion, result.getSuggestions().get(0));
    }
    
    @Test
    @DisplayName("Should handle null lists gracefully")
    void shouldHandleNullListsGracefully() {
        CodeAnalysisResult result = new CodeAnalysisResult(null, null, 90.0);
        
        assertNotNull(result.getErrors());
        assertNotNull(result.getSuggestions());
        assertTrue(result.getErrors().isEmpty());
        assertTrue(result.getSuggestions().isEmpty());
        assertEquals(90.0, result.getScore());
    }
    
    @Test
    @DisplayName("Should correctly report if has issues")
    void shouldCorrectlyReportIfHasIssues() {
        // Initially no issues
        assertFalse(result.hasIssues());
        
        // Add an error
        CodeIssue error = new CodeIssue("Error", 1, CodeIssue.Type.ERROR);
        result.getErrors().add(error);
        assertTrue(result.hasIssues());
        
        // Clear and add a suggestion
        result.getErrors().clear();
        CodeIssue suggestion = new CodeIssue("Suggestion", 2, CodeIssue.Type.SUGGESTION);
        result.getSuggestions().add(suggestion);
        assertTrue(result.hasIssues());
    }
    
    @Test
    @DisplayName("Should correctly count total issues")
    void shouldCorrectlyCountTotalIssues() {
        assertEquals(0, result.getTotalIssueCount());
        
        // Add errors and suggestions
        result.getErrors().add(new CodeIssue("Error 1", 1, CodeIssue.Type.ERROR));
        result.getErrors().add(new CodeIssue("Error 2", 2, CodeIssue.Type.ERROR));
        result.getSuggestions().add(new CodeIssue("Suggestion 1", 3, CodeIssue.Type.SUGGESTION));
        
        assertEquals(3, result.getTotalIssueCount());
    }
    
    @Test
    @DisplayName("Should set and get analysis time")
    void shouldSetAndGetAnalysisTime() {
        String analysisTime = "2024-08-15T10:30:00Z";
        result.setAnalysisTime(analysisTime);
        assertEquals(analysisTime, result.getAnalysisTime());
    }
    
    @Test
    @DisplayName("Should set and get score")
    void shouldSetAndGetScore() {
        result.setScore(75.5);
        assertEquals(75.5, result.getScore());
    }
    
    @Test
    @DisplayName("Should set errors list")
    void shouldSetErrorsList() {
        CodeIssue error1 = new CodeIssue("Error 1", 1, CodeIssue.Type.ERROR);
        CodeIssue error2 = new CodeIssue("Error 2", 2, CodeIssue.Type.ERROR);
        List<CodeIssue> errors = Arrays.asList(error1, error2);
        
        result.setErrors(errors);
        
        assertEquals(2, result.getErrors().size());
        assertEquals(error1, result.getErrors().get(0));
        assertEquals(error2, result.getErrors().get(1));
    }
    
    @Test
    @DisplayName("Should set suggestions list")
    void shouldSetSuggestionsList() {
        CodeIssue suggestion1 = new CodeIssue("Suggestion 1", 1, CodeIssue.Type.SUGGESTION);
        CodeIssue suggestion2 = new CodeIssue("Suggestion 2", 2, CodeIssue.Type.SUGGESTION);
        List<CodeIssue> suggestions = Arrays.asList(suggestion1, suggestion2);
        
        result.setSuggestions(suggestions);
        
        assertEquals(2, result.getSuggestions().size());
        assertEquals(suggestion1, result.getSuggestions().get(0));
        assertEquals(suggestion2, result.getSuggestions().get(1));
    }
}
