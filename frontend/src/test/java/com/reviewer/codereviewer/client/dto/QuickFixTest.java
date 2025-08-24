package com.reviewer.codereviewer.client.dto;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Quick Fix DTO Tests")
class QuickFixTest {
    
    private QuickFix quickFix;
    
    @BeforeEach
    void setUp() {
        quickFix = new QuickFix();
    }
    
    @Test
    @DisplayName("Should initialize with default constructor")
    void shouldInitializeWithDefaultConstructor() {
        assertNotNull(quickFix);
        assertNull(quickFix.getDescription());
        assertNull(quickFix.getAction());
        assertNull(quickFix.getReplacement());
        assertEquals(0, quickFix.getStartLine());
        assertEquals(0, quickFix.getEndLine());
    }
    
    @Test
    @DisplayName("Should initialize with description and action")
    void shouldInitializeWithDescriptionAndAction() {
        String description = "Fix unused import";
        String action = "Remove import statement";
        
        QuickFix fix = new QuickFix(description, action);
        
        assertEquals(description, fix.getDescription());
        assertEquals(action, fix.getAction());
        assertNull(fix.getReplacement());
        assertEquals(0, fix.getStartLine());
        assertEquals(0, fix.getEndLine());
    }
    
    @Test
    @DisplayName("Should initialize with all parameters")
    void shouldInitializeWithAllParameters() {
        String description = "Replace with constant";
        String action = "Replace magic number";
        String replacement = "public static final int MAX_SIZE = 100;";
        int startLine = 5;
        int endLine = 5;
        
        QuickFix fix = new QuickFix(description, action, replacement, startLine, endLine);
        
        assertEquals(description, fix.getDescription());
        assertEquals(action, fix.getAction());
        assertEquals(replacement, fix.getReplacement());
        assertEquals(startLine, fix.getStartLine());
        assertEquals(endLine, fix.getEndLine());
    }
    
    @Test
    @DisplayName("Should set and get description")
    void shouldSetAndGetDescription() {
        String description = "Test description";
        quickFix.setDescription(description);
        assertEquals(description, quickFix.getDescription());
    }
    
    @Test
    @DisplayName("Should set and get action")
    void shouldSetAndGetAction() {
        String action = "Test action";
        quickFix.setAction(action);
        assertEquals(action, quickFix.getAction());
    }
    
    @Test
    @DisplayName("Should set and get replacement")
    void shouldSetAndGetReplacement() {
        String replacement = "Test replacement code";
        quickFix.setReplacement(replacement);
        assertEquals(replacement, quickFix.getReplacement());
    }
    
    @Test
    @DisplayName("Should set and get start line")
    void shouldSetAndGetStartLine() {
        int startLine = 10;
        quickFix.setStartLine(startLine);
        assertEquals(startLine, quickFix.getStartLine());
    }
    
    @Test
    @DisplayName("Should set and get end line")
    void shouldSetAndGetEndLine() {
        int endLine = 15;
        quickFix.setEndLine(endLine);
        assertEquals(endLine, quickFix.getEndLine());
    }
    
    @Test
    @DisplayName("ToString should return description when available")
    void toStringShouldReturnDescriptionWhenAvailable() {
        String description = "Test description";
        quickFix.setDescription(description);
        
        assertEquals(description, quickFix.toString());
    }
    
    @Test
    @DisplayName("ToString should return action when description is null")
    void toStringShouldReturnActionWhenDescriptionIsNull() {
        String action = "Test action";
        quickFix.setAction(action);
        
        assertEquals(action, quickFix.toString());
    }
    
    @Test
    @DisplayName("ToString should return action when description is empty")
    void toStringShouldReturnActionWhenDescriptionIsEmpty() {
        String action = "Test action";
        quickFix.setDescription("");
        quickFix.setAction(action);
        
        assertEquals(action, quickFix.toString());
    }
    
    @Test
    @DisplayName("ToString should handle null values gracefully")
    void toStringShouldHandleNullValuesGracefully() {
        String result = quickFix.toString();
        assertNotNull(result);
        // Should not throw an exception
    }
    
    @Test
    @DisplayName("Should handle line range correctly")
    void shouldHandleLineRangeCorrectly() {
        quickFix.setStartLine(10);
        quickFix.setEndLine(15);
        
        assertEquals(10, quickFix.getStartLine());
        assertEquals(15, quickFix.getEndLine());
        assertTrue(quickFix.getEndLine() >= quickFix.getStartLine());
    }
    
    @Test
    @DisplayName("Should handle single line fix")
    void shouldHandleSingleLineFix() {
        quickFix.setStartLine(10);
        quickFix.setEndLine(10);
        
        assertEquals(10, quickFix.getStartLine());
        assertEquals(10, quickFix.getEndLine());
        assertEquals(quickFix.getStartLine(), quickFix.getEndLine());
    }
}
