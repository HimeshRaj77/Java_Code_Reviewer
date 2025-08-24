package com.project.codereviewer.service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import com.project.codereviewer.config.AppConfig;

/**
 * Service for interacting with AI APIs to get code suggestions.
 * Uses OpenRouter to access various open-source models like Mistral or LLaMA 3.
 */
@Service
public class AISuggestionService {
    private static final String API_URL = "https://openrouter.ai/api/v1/chat/completions";
    private static final Duration TIMEOUT = Duration.ofSeconds(120); // Increased timeout to 120 seconds
    private static final String DEFAULT_MODEL = "mistralai/mixtral-8x7b-instruct";
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
        logger.info("Requesting code suggestion for review");
        String apiKey = AppConfig.getOpenRouterApiKey();
        String model = AppConfig.getOpenRouterModelName(DEFAULT_MODEL);

        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("model", model);
            
            JSONArray messages = new JSONArray();
            messages.put(new JSONObject().put("role", "system").put("content", SYSTEM_PROMPT));
            messages.put(new JSONObject().put("role", "user").put("content", codeToReview));
            requestBody.put("messages", messages);
            
            // Configure streaming response
            requestBody.put("stream", false);
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .header("HTTP-Referer", "https://github.com/java-code-reviewer")
                    .header("X-Title", "Java Code Reviewer")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                String errorMsg = "API error: " + response.statusCode() + " - " + response.body();
                logger.severe(errorMsg);
                throw new IOException(errorMsg);
            }

            JSONObject responseBody = new JSONObject(response.body());
            String content = extractContentFromResponse(responseBody);
            logger.info("Successfully received AI suggestion");
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
        
        // First, inform the user that we're starting the request
        responseConsumer.accept("Starting AI review request...\n\n");
        
        String apiKey;
        try {
            logger.info("Attempting to get OpenRouter API key");
            apiKey = AppConfig.getOpenRouterApiKey();
            logger.info("Successfully retrieved API key with length: " + (apiKey != null ? apiKey.length() : 0));
            
            // Quick validation of API key
            if (apiKey == null || apiKey.trim().isEmpty()) {
                throw new IOException("API key is empty or null");
            }
            if (!apiKey.startsWith("sk-")) {
                logger.warning("API key doesn't start with 'sk-', which might indicate an invalid format");
            }
        } catch (IOException e) {
            logger.severe("Failed to get API key: " + e.getMessage());
            e.printStackTrace();
            responseConsumer.accept("Error: Failed to get API key - " + e.getMessage() + "\n" +
                                   "Please check your secrets.properties file and ensure the OpenRouter API key is correctly set.");
            future.completeExceptionally(e);
            return future;
        }
        
        String model = AppConfig.getOpenRouterModelName(DEFAULT_MODEL);
        logger.info("Using model: " + model + " for streaming response");
        responseConsumer.accept("Using AI model: " + model + "\n\n");
        
        // Validate code length - large inputs can cause timeouts
        int codeLength = codeToReview != null ? codeToReview.length() : 0;
        logger.info("Code length for review: " + codeLength + " characters");
        if (codeLength > 20000) {
            logger.warning("Very large code submission (" + codeLength + " chars) may cause slow response");
            responseConsumer.accept("Note: The code submission is large (" + (codeLength/1000) + "KB), which may result in a slower response.\n\n");
        }
        
        JSONObject requestBody = new JSONObject();
        requestBody.put("model", model);
        
        JSONArray messages = new JSONArray();
        messages.put(new JSONObject().put("role", "system").put("content", SYSTEM_PROMPT));
        messages.put(new JSONObject().put("role", "user").put("content", codeToReview));
        requestBody.put("messages", messages);
        
        // Configure streaming response with max_tokens to potentially speed up response
        requestBody.put("stream", true);
        requestBody.put("max_tokens", 2000); // Limit response length to potentially speed up completion
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .header("HTTP-Referer", "https://github.com/java-code-reviewer")
                .header("X-Title", "Java Code Reviewer")
                .timeout(TIMEOUT) // Explicitly set the timeout for this request
                .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                .build();
        
        logger.info("Sending API request to: " + API_URL);
        logger.info("Request headers: Content-Type=application/json, Authorization=Bearer <API_KEY>, HTTP-Referer, X-Title");
        logger.info("Request body length: " + requestBody.toString().length() + " characters");
        logger.info("Stream mode: " + requestBody.getBoolean("stream"));
        
        // Add a timeout handler to detect if the request is taking too long
        final long timeoutMillis = TIMEOUT.toMillis();
        
        // Start a timer to check for timeout
        new Thread(() -> {
            try {
                Thread.sleep(timeoutMillis / 2); // Check halfway through timeout
                if (!future.isDone()) {
                    logger.warning("Request is taking longer than expected (" + (timeoutMillis / 2000) + " seconds)");
                    responseConsumer.accept("\n[Still waiting for response...]\n");
                }
                
                Thread.sleep(timeoutMillis / 2); // Check at full timeout
                if (!future.isDone()) {
                    logger.severe("Request likely timed out after " + (timeoutMillis / 1000) + " seconds");
                    responseConsumer.accept("\n\n⚠️ Warning: Request is taking unusually long. The service may be experiencing issues.\n");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
        
        httpClient.sendAsync(request, HttpResponse.BodyHandlers.fromLineSubscriber(new Flow.Subscriber<>() {
            private Flow.Subscription subscription;
            private final StringBuilder fullResponse = new StringBuilder();
            // Track if we've received any actual content
            private long lastContentTime = System.currentTimeMillis();
            
            @Override
            public void onSubscribe(Flow.Subscription subscription) {
                this.subscription = subscription;
                subscription.request(Long.MAX_VALUE);
            }

            @Override
            public void onNext(String line) {
                // Update the last content time whenever we receive any data
                lastContentTime = System.currentTimeMillis();
                
                if (line.isEmpty()) {
                    return;
                }
                
                if (line.equals("data: [DONE]")) {
                    logger.info("Received stream completion marker");
                    // Check if we got any content at all
                    if (fullResponse.length() == 0) {
                        logger.warning("Stream completed but no content was received");
                        responseConsumer.accept("\n\nWarning: No content was received from the AI model. This may indicate an issue with the model or API.");
                    }
                    return;
                }
                
                // Log the raw line occasionally for debugging
                if (fullResponse.length() == 0) {
                    logger.info("First response line received: " + line);
                }
                
                if (line.startsWith("data: ")) {
                    String jsonData = line.substring(6);
                    
                    // Handle error responses
                    if (jsonData.contains("error") && !jsonData.contains("delta")) {
                        try {
                            JSONObject errorObj = new JSONObject(jsonData);
                            if (errorObj.has("error")) {
                                String errorMessage = errorObj.getJSONObject("error").optString("message", "Unknown error");
                                logger.severe("API error response: " + errorMessage);
                                responseConsumer.accept("\n\nAPI Error: " + errorMessage);
                                future.completeExceptionally(new IOException("API error: " + errorMessage));
                                subscription.cancel();
                                return;
                            }
                        } catch (Exception e) {
                            logger.warning("Could not parse potential error response: " + jsonData);
                        }
                    }
                    
                    try {
                        JSONObject chunk = new JSONObject(jsonData);
                        if (chunk.has("choices") && !chunk.getJSONArray("choices").isEmpty()) {
                            JSONObject choice = chunk.getJSONArray("choices").getJSONObject(0);
                            
                            // Check for finish_reason which might indicate why the model stopped
                            if (choice.has("finish_reason") && !choice.isNull("finish_reason")) {
                                String reason = choice.getString("finish_reason");
                                logger.info("Model finished generation with reason: " + reason);
                                if (!"stop".equals(reason)) {
                                    responseConsumer.accept("\n\nNote: Model stopped generating with reason: " + reason);
                                }
                            }
                            
                            if (choice.has("delta") && choice.getJSONObject("delta").has("content")) {
                                String contentChunk = choice.getJSONObject("delta").getString("content");
                                responseConsumer.accept(contentChunk);
                                fullResponse.append(contentChunk);
                                // Mark that we've received actual content
                                if (fullResponse.length() == contentChunk.length()) {
                                    logger.info("Received first content chunk");
                                }
                            }
                        } else {
                            // If we have a response without choices, log it for debugging
                            logger.fine("Received response chunk without content: " + jsonData);
                        }
                    } catch (Exception e) {
                        logger.log(Level.SEVERE, "Error parsing streaming response: " + jsonData, e);
                        responseConsumer.accept("\n\nError parsing response: " + e.getMessage());
                        future.completeExceptionally(new IOException("Error parsing streaming response: " + e.getMessage(), e));
                        subscription.cancel();
                    }
                } else {
                    // Log unexpected response format
                    logger.warning("Received unexpected response format: " + line);
                }
            }

            @Override
            public void onError(Throwable throwable) {
                logger.log(Level.SEVERE, "Error in streaming response", throwable);
                responseConsumer.accept("\n\n⚠️ Error: The AI service encountered an error: " + throwable.getMessage() + 
                                      "\nPlease try again later or check your API key and internet connection.");
                future.completeExceptionally(throwable);
            }

            @Override
            public void onComplete() {
                logger.info("Streaming response completed successfully");
                responseConsumer.accept("\n\n(Review complete)");
                future.complete(null);
            }
        })).exceptionally(ex -> {
            logger.log(Level.SEVERE, "Exception during HTTP request", ex);
            responseConsumer.accept("\n\n⚠️ Network Error: Failed to connect to AI service: " + ex.getMessage() + 
                                  "\nPossible causes: Internet connectivity issue, API service unavailable, or timeout.");
            future.completeExceptionally(ex);
            return null;
        });
        
        return future;
    }
    
    /**
     * Extract content from the API response.
     */
    private String extractContentFromResponse(JSONObject response) {
        if (response.has("choices") && !response.getJSONArray("choices").isEmpty()) {
            JSONObject choice = response.getJSONArray("choices").getJSONObject(0);
            if (choice.has("message") && choice.getJSONObject("message").has("content")) {
                return choice.getJSONObject("message").getString("content");
            }
        }
        return "No suggestion available.";
    }
}
