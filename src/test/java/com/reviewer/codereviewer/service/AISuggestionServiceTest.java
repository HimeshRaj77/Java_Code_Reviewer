package com.reviewer.codereviewer.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

public class AISuggestionServiceTest {

    private AISuggestionService aiSuggestionService;

    @BeforeEach
    void setUp() {
        aiSuggestionService = new AISuggestionService();
    }

    @Test
    @Disabled("Requires API key and makes actual API calls")
    void testGetCodeSuggestion() throws IOException {
        // This test makes actual API calls and requires an API key
        // Enable only when needed for integration testing
        String code = "public class Example { public void method() { int x = 5; } }";
        
        String result = aiSuggestionService.getCodeSuggestion(code);
        
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }
    
    @Test
    @Disabled("Requires API key and makes actual API calls")
    void testGetCodeSuggestionStreaming() {
        // This test makes actual API calls and requires an API key
        // Enable only when needed for integration testing
        String code = "public class Example { public void method() { int x = 5; } }";
        StringBuilder result = new StringBuilder();
        
        CompletableFuture<Void> future = aiSuggestionService.getCodeSuggestionStreaming(
            code, 
            chunk -> result.append(chunk)
        );
        
        future.join(); // Wait for completion
        
        assertNotNull(result.toString());
        assertFalse(result.toString().isEmpty());
    }
    
    @Test
    void testExtractContentFromResponse() throws Exception {
        // Test the private method using reflection
        java.lang.reflect.Method method = AISuggestionService.class.getDeclaredMethod(
            "extractContentFromResponse", org.json.JSONObject.class);
        method.setAccessible(true);
        
        // Create a test response object
        org.json.JSONObject response = new org.json.JSONObject(
            "{\"choices\":[{\"message\":{\"content\":\"Test suggestion\"}}]}"
        );
        
        String result = (String) method.invoke(aiSuggestionService, response);
        assertEquals("Test suggestion", result);
        
        // Test with empty response
        org.json.JSONObject emptyResponse = new org.json.JSONObject("{}");
        String emptyResult = (String) method.invoke(aiSuggestionService, emptyResponse);
        assertEquals("No suggestion available.", emptyResult);
    }
}
