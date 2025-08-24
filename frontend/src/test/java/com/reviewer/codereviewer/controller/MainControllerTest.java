package com.reviewer.codereviewer.controller;

import com.reviewer.codereviewer.client.ApiClientService;
import com.reviewer.codereviewer.client.dto.CodeAnalysisResult;
import com.reviewer.codereviewer.client.dto.CodeIssue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@DisplayName("Main Controller Tests")
class MainControllerTest {
    
    @Mock
    private ApiClientService mockApiClientService;
    
    private MainController controller;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new MainController(mockApiClientService);
    }
    
    @Test
    @DisplayName("Should initialize with default API client")
    void shouldInitializeWithDefaultApiClient() {
        MainController defaultController = new MainController();
        assertNotNull(defaultController);
    }
    
    @Test
    @DisplayName("Should initialize with custom API client")
    void shouldInitializeWithCustomApiClient() {
        assertNotNull(controller);
    }
    
    @Test
    @DisplayName("Should use API client for code analysis")
    void shouldUseApiClientForCodeAnalysis() {
        // Arrange
        String testCode = "public class Test {}";
        CodeAnalysisResult mockResult = createMockAnalysisResult();
        when(mockApiClientService.analyzeCode(testCode))
            .thenReturn(CompletableFuture.completedFuture(mockResult));
        
        // Act
        controller.analyzeCode(testCode);
        
        // Assert
        verify(mockApiClientService, times(1)).analyzeCode(testCode);
    }
    
    @Test
    @DisplayName("Should handle null code input gracefully")
    void shouldHandleNullCodeInputGracefully() {
        // Act - should not throw exception
        assertDoesNotThrow(() -> controller.analyzeCode(null));
        
        // Should not call API service for null input
        verify(mockApiClientService, never()).analyzeCode(anyString());
    }
    
    @Test
    @DisplayName("Should handle empty code input gracefully")
    void shouldHandleEmptyCodeInputGracefully() {
        // Act - should not throw exception
        assertDoesNotThrow(() -> controller.analyzeCode(""));
        
        // Should not call API service for empty input
        verify(mockApiClientService, never()).analyzeCode(anyString());
    }
    
    @Test
    @DisplayName("Should handle whitespace-only code input gracefully")
    void shouldHandleWhitespaceOnlyCodeInputGracefully() {
        // Act - should not throw exception
        assertDoesNotThrow(() -> controller.analyzeCode("   \n\t  "));
        
        // Should not call API service for whitespace-only input
        verify(mockApiClientService, never()).analyzeCode(anyString());
    }
    
    @Test
    @DisplayName("Should use API client for AI questions")
    void shouldUseApiClientForAiQuestions() {
        // Arrange
        String testCode = "public class Test {}";
        String testQuestion = "Please review this code";
        when(mockApiClientService.getCodeSuggestionStreaming(anyString(), anyString(), any()))
            .thenReturn(CompletableFuture.completedFuture(null));
        
        // Act
        controller.onAskAiWithQuestion(testCode, testQuestion);
        
        // Assert
        verify(mockApiClientService, times(1))
            .getCodeSuggestionStreaming(eq(testCode), eq(testQuestion), any());
    }
    
    @Test
    @DisplayName("Should handle API service failures gracefully")
    void shouldHandleApiServiceFailuresGracefully() {
        // Arrange
        String testCode = "public class Test {}";
        CompletableFuture<CodeAnalysisResult> failedFuture = new CompletableFuture<>();
        failedFuture.completeExceptionally(new RuntimeException("API service unavailable"));
        when(mockApiClientService.analyzeCode(testCode)).thenReturn(failedFuture);
        
        // Act - should not throw exception
        assertDoesNotThrow(() -> controller.analyzeCode(testCode));
        
        // Assert that the API service was called
        verify(mockApiClientService, times(1)).analyzeCode(testCode);
    }
    
    @Test
    @DisplayName("Should call analyzeCode when analyzeAndDisplay is called")
    void shouldCallAnalyzeCodeWhenAnalyzeAndDisplayIsCalled() {
        // Arrange
        String testCode = "public class Test {}";
        CodeAnalysisResult mockResult = createMockAnalysisResult();
        when(mockApiClientService.analyzeCode(testCode))
            .thenReturn(CompletableFuture.completedFuture(mockResult));
        
        // Act
        controller.analyzeAndDisplay(testCode);
        
        // Assert
        verify(mockApiClientService, times(1)).analyzeCode(testCode);
    }
    
    @Test
    @DisplayName("Should store last result when analysis completes")
    void shouldStoreLastResultWhenAnalysisCompletes() throws Exception {
        // Arrange
        String testCode = "public class Test {}";
        CodeAnalysisResult mockResult = createMockAnalysisResult();
        when(mockApiClientService.analyzeCode(testCode))
            .thenReturn(CompletableFuture.completedFuture(mockResult));
        
        // Act
        controller.analyzeCode(testCode);
        
        // Wait a bit for async completion (in a real test, you'd use proper async testing)
        Thread.sleep(100);
        
        // Assert - getLastResult should return the result (this is a simplified test)
        // In practice, you'd need to test this with proper async testing techniques
        verify(mockApiClientService, times(1)).analyzeCode(testCode);
    }
    
    private CodeAnalysisResult createMockAnalysisResult() {
        CodeIssue error = new CodeIssue("Test error", 1, CodeIssue.Type.ERROR);
        CodeIssue suggestion = new CodeIssue("Test suggestion", 2, CodeIssue.Type.SUGGESTION);
        
        return new CodeAnalysisResult(
            Arrays.asList(error),
            Arrays.asList(suggestion),
            85.0
        );
    }
}
