package com.reviewer.codereviewer.client;

import com.reviewer.codereviewer.client.dto.CodeAnalysisResult;
import com.reviewer.codereviewer.client.dto.CodeIssue;
import com.reviewer.codereviewer.client.dto.QuickFix;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.List;
import java.util.ArrayList;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * API Client Service for communicating with the CodeReviewer backend server.
 * This service handles all HTTP communication and serialization/deserialization.
 */
public class ApiClientService {
    private static final Logger logger = Logger.getLogger(ApiClientService.class.getName());
    
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String serverBaseUrl;
    
    // Default configuration
    private static final String DEFAULT_SERVER_URL = "http://localhost:8080";
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(30);
    private static final Duration STREAMING_TIMEOUT = Duration.ofMinutes(2);
    
    public ApiClientService() {
        this(DEFAULT_SERVER_URL);
    }
    
    public ApiClientService(String serverBaseUrl) {
        this.serverBaseUrl = serverBaseUrl.endsWith("/") ? 
            serverBaseUrl.substring(0, serverBaseUrl.length() - 1) : serverBaseUrl;
        
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
            
        this.objectMapper = new ObjectMapper();
        
        logger.info("ApiClientService initialized with server URL: " + this.serverBaseUrl);
    }
    
    /**
     * Analyzes the provided code by sending it to the backend server.
     * 
     * @param code The source code to analyze
     * @return CompletableFuture containing the analysis result
     */
    public CompletableFuture<CodeAnalysisResult> analyzeCode(String code) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.info("Sending code analysis request to server");
                
                // Create request body
                String requestBody = objectMapper.writeValueAsString(new CodeAnalysisRequest(code));
                
                // Build HTTP request
                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(serverBaseUrl + "/api/code-review/analyze"))
                    .timeout(REQUEST_TIMEOUT)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();
                
                // Send request and handle response
                HttpResponse<String> response = httpClient.send(request, 
                    HttpResponse.BodyHandlers.ofString());
                
                if (response.statusCode() == 200) {
                    logger.info("Analysis request successful");
                    return parseAnalysisResult(response.body());
                } else {
                    logger.warning("Analysis request failed with status: " + response.statusCode());
                    throw new RuntimeException("Server error: " + response.statusCode() + " - " + response.body());
                }
                
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error during code analysis", e);
                throw new RuntimeException("Failed to analyze code: " + e.getMessage(), e);
            }
        });
    }
    
    /**
     * Gets AI suggestions for the provided code with streaming response.
     * 
     * @param code The source code to get suggestions for
     * @param question The specific question or request about the code
     * @param chunkConsumer Consumer that receives chunks of the streaming response
     * @return CompletableFuture that completes when streaming is done
     */
    public CompletableFuture<Void> getCodeSuggestionStreaming(String code, String question, Consumer<String> chunkConsumer) {
        return CompletableFuture.runAsync(() -> {
            try {
                logger.info("Sending AI suggestion request to server");
                
                // Create request body
                AISuggestionRequest requestObj = new AISuggestionRequest(code, question);
                String requestBody = objectMapper.writeValueAsString(requestObj);
                
                // Build HTTP request
                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(serverBaseUrl + "/api/code-review/ai-suggest"))
                    .timeout(STREAMING_TIMEOUT)
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();
                
                // Send request and handle JSON response
                HttpResponse<String> response = httpClient.send(request, 
                    HttpResponse.BodyHandlers.ofString());
                
                if (response.statusCode() == 200) {
                    logger.info("AI suggestion request successful, processing response");
                    // Parse JSON response to extract suggestion
                    JsonNode jsonResponse = objectMapper.readTree(response.body());
                    String suggestion = jsonResponse.get("suggestion").asText();
                    logger.info("Received suggestion from backend, length: " + suggestion.length() + " chars");
                    logger.info("Suggestion content: >>>\n" + suggestion + "\n<<< END suggestion");
                    // Send the complete suggestion to the consumer
                    chunkConsumer.accept(suggestion);
                } else {
                    logger.warning("AI suggestion request failed with status: " + response.statusCode());
                    String errorBody = response.body();
                    throw new RuntimeException("Server error: " + response.statusCode() + " - " + errorBody);
                }
                
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error during AI suggestion request", e);
                throw new RuntimeException("Failed to get AI suggestions: " + e.getMessage(), e);
            }
        });
    }
    
    /**
     * Simple AI suggestion method that returns the complete response.
     * 
     * @param code The source code to get suggestions for
     * @param question The specific question or request about the code
     * @return CompletableFuture containing the complete AI response
     */
    public CompletableFuture<String> getCodeSuggestion(String code, String question) {
        CompletableFuture<String> future = new CompletableFuture<>();
        StringBuilder responseBuilder = new StringBuilder();
        
        getCodeSuggestionStreaming(code, question, chunk -> {
            responseBuilder.append(chunk);
        }).whenComplete((v, ex) -> {
            if (ex != null) {
                future.completeExceptionally(ex);
            } else {
                future.complete(responseBuilder.toString());
            }
        });
        
        return future;
    }
    
    /**
     * Checks if the server is available and responding.
     * 
     * @return CompletableFuture containing true if server is healthy, false otherwise
     */
    public CompletableFuture<Boolean> checkServerHealth() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(serverBaseUrl + "/api/code-review/health"))
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();
                
                HttpResponse<String> response = httpClient.send(request, 
                    HttpResponse.BodyHandlers.ofString());
                
                return response.statusCode() == 200;
            } catch (Exception e) {
                logger.log(Level.WARNING, "Server health check failed", e);
                return false;
            }
        });
    }
    
    /**
     * Parses the JSON response from the analysis endpoint into a CodeAnalysisResult object.
     */
    private CodeAnalysisResult parseAnalysisResult(String jsonResponse) throws Exception {
        JsonNode rootNode = objectMapper.readTree(jsonResponse);
        
        CodeAnalysisResult result = new CodeAnalysisResult();
        
        // Parse errors
        if (rootNode.has("errors")) {
            List<CodeIssue> errors = new ArrayList<>();
            for (JsonNode errorNode : rootNode.get("errors")) {
                errors.add(parseCodeIssue(errorNode));
            }
            result.setErrors(errors);
        }
        
        // Parse suggestions
        if (rootNode.has("suggestions")) {
            List<CodeIssue> suggestions = new ArrayList<>();
            for (JsonNode suggestionNode : rootNode.get("suggestions")) {
                suggestions.add(parseCodeIssue(suggestionNode));
            }
            result.setSuggestions(suggestions);
        }
        
        // Parse score
        if (rootNode.has("score")) {
            result.setScore(rootNode.get("score").asDouble());
        }
        
        // Parse analysis time
        if (rootNode.has("analysisTime")) {
            result.setAnalysisTime(rootNode.get("analysisTime").asText());
        }
        
        return result;
    }
    
    /**
     * Parses a JSON node into a CodeIssue object.
     */
    private CodeIssue parseCodeIssue(JsonNode issueNode) {
        CodeIssue issue = new CodeIssue();
        
        if (issueNode.has("message")) {
            issue.setMessage(issueNode.get("message").asText());
        }
        
        if (issueNode.has("line")) {
            issue.setLine(issueNode.get("line").asInt());
        }
        
        if (issueNode.has("column")) {
            issue.setColumn(issueNode.get("column").asInt());
        }
        
        if (issueNode.has("type")) {
            String typeStr = issueNode.get("type").asText().toUpperCase();
            try {
                issue.setType(CodeIssue.Type.valueOf(typeStr));
            } catch (IllegalArgumentException e) {
                issue.setType(CodeIssue.Type.INFO);
            }
        }
        
        if (issueNode.has("severity")) {
            issue.setSeverity(issueNode.get("severity").asText());
        }
        
        // Parse quick fixes if present
        if (issueNode.has("quickFixes")) {
            List<QuickFix> quickFixes = new ArrayList<>();
            for (JsonNode fixNode : issueNode.get("quickFixes")) {
                QuickFix quickFix = new QuickFix();
                if (fixNode.has("description")) {
                    quickFix.setDescription(fixNode.get("description").asText());
                }
                if (fixNode.has("action")) {
                    quickFix.setAction(fixNode.get("action").asText());
                }
                if (fixNode.has("replacement")) {
                    quickFix.setReplacement(fixNode.get("replacement").asText());
                }
                if (fixNode.has("startLine")) {
                    quickFix.setStartLine(fixNode.get("startLine").asInt());
                }
                if (fixNode.has("endLine")) {
                    quickFix.setEndLine(fixNode.get("endLine").asInt());
                }
                quickFixes.add(quickFix);
            }
            issue.setQuickFixes(quickFixes);
        }
        
        return issue;
    }
    
    // Inner classes for request DTOs
    private static class CodeAnalysisRequest {
        public final String code;
        
        public CodeAnalysisRequest(String code) {
            this.code = code;
        }
    }
    
    private static class AISuggestionRequest {
        public final String code;
        public final String question;
        
        public AISuggestionRequest(String code, String question) {
            this.code = code;
            this.question = question;
        }
    }
}
