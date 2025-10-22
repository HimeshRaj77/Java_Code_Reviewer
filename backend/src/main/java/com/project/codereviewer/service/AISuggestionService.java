package com.project.codereviewer.service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONObject;
import org.springframework.stereotype.Service;


/**
 * Service for interacting with AI APIs to get code suggestions.
 * Uses OpenRouter to access various open-source models like Mistral or LLaMA 3.
 */
@Service
public class AISuggestionService {
    private static final String API_URL = "http://localhost:11434/api/generate";
    private static final Duration TIMEOUT = Duration.ofSeconds(120);
    private static final String DEFAULT_MODEL = "mistral";
    private static final String SYSTEM_PROMPT = """
            You are an expert Java code reviewer with extensive experience in software architecture, design patterns, and performance optimization.
            Your task is to provide professional, actionable, and educational code reviews.

            FORMAT YOUR RESPONSE IN MARKDOWN WITH THE FOLLOWING STRICT STRUCTURE:
            
            ## Overall Summary
            - Brief overview of the code's purpose and architecture
            - Key strengths and areas for improvement
            - Maximum 3-4 sentences
            
            ## Critical Issues (Potential Bugs)
            - List only genuine bugs, race conditions, security vulnerabilities, or critical performance issues
            - Each issue must include:
              * Line number reference
              * Clear explanation of the potential impact
              * Specific code fix recommendation
            
            ## Suggestions for Improvement
            For each suggestion:
            ```
            [Line XX] - Component/Issue Title
            WHY: Clear explanation of why this needs improvement
            RECOMMENDATION: 
            ```java
            // Corrected code snippet
            ```
            ```
            
            ## Best Practices & Code Compliments
            - Acknowledge what was done well
            - Suggest relevant design patterns or Java best practices if applicable
            - Maximum 2-3 specific points
            
            CONSTRAINTS:
            - Focus on substantial issues, not trivial style preferences
            - Be constructive and educational in tone
            - Always provide concrete examples and explanations
            - Include code snippets for non-trivial changes
            - Reference specific line numbers for all suggestions
            - Prioritize:
              * Thread safety and concurrent operation issues
              * Resource management (memory, file handles, etc.)
              * API design and method signatures
              * Error handling and edge cases
              * Performance optimization opportunities
            """;
    
    private final HttpClient httpClient;
    private final Logger logger = Logger.getLogger(AISuggestionService.class.getName());
    
    public AISuggestionService() {
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(TIMEOUT)
                .build();
        logger.info("AISuggestionService initialized with timeout: " + TIMEOUT.getSeconds() + " seconds");
    }
    
    /**
     * Get AI suggestions for code review in a blocking manner.
     * 
     * @param codeToReview The code to be reviewed
     * @return The AI suggestion as a String
     * @throws IOException If an error occurs during API communication
     */
    public String getCodeSuggestion(String codeToReview) throws IOException {
        logger.info("Requesting code suggestion for review (Ollama Mistral)");
        String model = DEFAULT_MODEL;
        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("model", model);
            requestBody.put("prompt", SYSTEM_PROMPT + "\n\n" + codeToReview);
            requestBody.put("stream", false);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                String errorMsg = "Ollama API error: " + response.statusCode() + " - " + response.body();
                logger.severe(errorMsg);
                throw new IOException(errorMsg);
            }

            JSONObject responseBody = new JSONObject(response.body());
            String content = responseBody.optString("response", "No suggestion available.");
            logger.info("Successfully received AI suggestion from Ollama");
            return content;
        } catch (InterruptedException e) {
            logger.log(Level.SEVERE, "Request interrupted", e);
            Thread.currentThread().interrupt();
            throw new IOException("Request interrupted: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to get AI suggestion", e);
            throw new IOException("Failed to get AI suggestion: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get AI suggestions for code review with streaming response.
     * 
     * @param codeToReview The code to be reviewed
     * @param responseConsumer Consumer that processes each chunk of the streaming response
     * @return CompletableFuture that completes when the entire stream is processed
     */
    public CompletableFuture<Void> getCodeSuggestionStreaming(String codeToReview, Consumer<String> responseConsumer) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        responseConsumer.accept("Starting AI review request (Ollama Mistral)...\n\n");
        String model = DEFAULT_MODEL;
        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("model", model);
            requestBody.put("prompt", SYSTEM_PROMPT + "\n\n" + codeToReview);
            requestBody.put("stream", false);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                    .build();

            httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    if (response.statusCode() != 200) {
                        String errorMsg = "Ollama API error: " + response.statusCode() + " - " + response.body();
                        logger.severe(errorMsg);
                        responseConsumer.accept(errorMsg);
                        future.completeExceptionally(new IOException(errorMsg));
                        return;
                    }
                    JSONObject responseBody = new JSONObject(response.body());
                    String content = responseBody.optString("response", "No suggestion available.");
                    responseConsumer.accept(content);
                    future.complete(null);
                })
                .exceptionally(ex -> {
                    logger.log(Level.SEVERE, "Exception during HTTP request", ex);
                    responseConsumer.accept("\n\n⚠️ Network Error: Failed to connect to Ollama service: " + ex.getMessage());
                    future.completeExceptionally(ex);
                    return null;
                });
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to get AI suggestion (Ollama)", e);
            responseConsumer.accept("Error: Failed to get AI suggestion (Ollama) - " + e.getMessage());
            future.completeExceptionally(e);
        }
        return future;
    }
    
    // No longer needed: extractContentFromResponse
}
