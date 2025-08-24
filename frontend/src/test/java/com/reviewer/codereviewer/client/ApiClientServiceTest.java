package com.reviewer.codereviewer.client;

import com.reviewer.codereviewer.client.dto.CodeAnalysisResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Disabled;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("API Client Service Tests")
class ApiClientServiceTest {
    
    private ApiClientService apiClientService;
    
    @BeforeEach
    void setUp() {
        // Use default localhost:8080 for testing
        apiClientService = new ApiClientService();
    }
    
    @Test
    @DisplayName("Should initialize with default server URL")
    void shouldInitializeWithDefaultUrl() {
        ApiClientService defaultClient = new ApiClientService();
        assertNotNull(defaultClient);
    }
    
    @Test
    @DisplayName("Should initialize with custom server URL")
    void shouldInitializeWithCustomUrl() {
        String customUrl = "http://localhost:9090";
        ApiClientService customClient = new ApiClientService(customUrl);
        assertNotNull(customClient);
    }
    
    @Test
    @DisplayName("Should handle trailing slash in server URL")
    void shouldHandleTrailingSlashInUrl() {
        String urlWithSlash = "http://localhost:8080/";
        ApiClientService client = new ApiClientService(urlWithSlash);
        assertNotNull(client);
        // The implementation should remove the trailing slash
    }
    
    @Test
    @Disabled("Requires running backend server")
    @DisplayName("Should successfully analyze code when server is available")
    void shouldAnalyzeCodeWhenServerAvailable() throws ExecutionException, InterruptedException, TimeoutException {
        String sampleCode = """
            public class TestClass {
                public void testMethod() {
                    System.out.println("Hello World");
                }
            }
            """;
        
        CompletableFuture<CodeAnalysisResult> future = apiClientService.analyzeCode(sampleCode);
        CodeAnalysisResult result = future.get(30, TimeUnit.SECONDS);
        
        assertNotNull(result);
        assertNotNull(result.getErrors());
        assertNotNull(result.getSuggestions());
    }
    
    @Test
    @Disabled("Requires running backend server")
    @DisplayName("Should check server health when server is available")
    void shouldCheckServerHealthWhenAvailable() throws ExecutionException, InterruptedException, TimeoutException {
        CompletableFuture<Boolean> future = apiClientService.checkServerHealth();
        Boolean isHealthy = future.get(10, TimeUnit.SECONDS);
        
        assertTrue(isHealthy);
    }
    
    @Test
    @DisplayName("Should handle server unavailable gracefully")
    void shouldHandleServerUnavailableGracefully() {
        // Use a non-existent server URL
        ApiClientService unavailableClient = new ApiClientService("http://localhost:99999");
        
        CompletableFuture<Boolean> healthFuture = unavailableClient.checkServerHealth();
        
        assertDoesNotThrow(() -> {
            Boolean isHealthy = healthFuture.get(10, TimeUnit.SECONDS);
            assertFalse(isHealthy);
        });
    }
    
    @Test
    @DisplayName("Should handle null code input gracefully")
    void shouldHandleNullCodeInput() {
        CompletableFuture<CodeAnalysisResult> future = apiClientService.analyzeCode(null);
        
        assertThrows(ExecutionException.class, () -> {
            future.get(5, TimeUnit.SECONDS);
        });
    }
    
    @Test
    @DisplayName("Should handle empty code input gracefully")
    void shouldHandleEmptyCodeInput() {
        CompletableFuture<CodeAnalysisResult> future = apiClientService.analyzeCode("");
        
        // Should not throw immediately (the server will handle empty input)
        assertNotNull(future);
        assertFalse(future.isDone() && future.isCompletedExceptionally());
    }
    
    @Test
    @Disabled("Requires running backend server with AI functionality")
    @DisplayName("Should stream AI suggestions when server is available")
    void shouldStreamAiSuggestionsWhenServerAvailable() throws ExecutionException, InterruptedException, TimeoutException {
        String sampleCode = """
            public class TestClass {
                public void testMethod() {
                    System.out.println("Hello World");
                }
            }
            """;
        String question = "Please review this code";
        StringBuilder response = new StringBuilder();
        
        CompletableFuture<Void> future = apiClientService.getCodeSuggestionStreaming(
            sampleCode, 
            question,
            chunk -> response.append(chunk)
        );
        
        future.get(60, TimeUnit.SECONDS);
        
        assertFalse(response.toString().isEmpty());
    }
    
    @Test
    @Disabled("Requires running backend server with AI functionality")
    @DisplayName("Should get complete AI suggestion when server is available")
    void shouldGetCompleteAiSuggestionWhenServerAvailable() throws ExecutionException, InterruptedException, TimeoutException {
        String sampleCode = """
            public class TestClass {
                public void testMethod() {
                    System.out.println("Hello World");
                }
            }
            """;
        String question = "Please review this code";
        
        CompletableFuture<String> future = apiClientService.getCodeSuggestion(sampleCode, question);
        String response = future.get(60, TimeUnit.SECONDS);
        
        assertNotNull(response);
        assertFalse(response.isEmpty());
    }
    
    @Test
    @DisplayName("Should handle AI request with null inputs gracefully")
    void shouldHandleAiRequestWithNullInputs() {
        CompletableFuture<String> future = apiClientService.getCodeSuggestion(null, null);
        
        assertThrows(ExecutionException.class, () -> {
            future.get(5, TimeUnit.SECONDS);
        });
    }
}
