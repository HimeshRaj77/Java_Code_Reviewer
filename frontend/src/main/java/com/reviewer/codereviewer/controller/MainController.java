package com.reviewer.codereviewer.controller;

import com.reviewer.codereviewer.client.ApiClientService;
import com.reviewer.codereviewer.client.dto.CodeAnalysisResult;
import com.reviewer.codereviewer.ui.MainView;
import com.reviewer.codereviewer.ui.ModernMainView;
import javafx.application.Platform;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainController {
    private static final Logger logger = Logger.getLogger(MainController.class.getName());
    
    private MainView mainView;
    private ModernMainView modernView;
    private final ApiClientService apiClientService;
    
    /**
     * Constructor with default API client
     */
    public MainController() {
        this.apiClientService = new ApiClientService();
    }
    
    /**
     * Constructor with custom API client (useful for testing)
     */
    public MainController(ApiClientService apiClientService) {
        this.apiClientService = apiClientService;
    }
    
    /**
     * Set the modern view for callbacks
     * @param modernView the modern view instance
     */
    public void setModernView(ModernMainView modernView) {
        this.modernView = modernView;
    }
    
    private CodeAnalysisResult lastResult;
    private String currentFileName = "";

    public void start(Stage primaryStage) {
        mainView = new MainView(this);
        mainView.start(primaryStage);
        
        // Check server health on startup
        checkServerConnection();
    }
    
    /**
     * Check if the backend server is available
     */
    private void checkServerConnection() {
        apiClientService.checkServerHealth().whenComplete((isHealthy, ex) -> {
            Platform.runLater(() -> {
                if (!isHealthy || ex != null) {
                    String errorMsg = "⚠️ Cannot connect to backend server. Please ensure the server is running on http://localhost:8080";
                    if (modernView != null) {
                        modernView.displayError(errorMsg);
                    } else if (mainView != null) {
                        mainView.displayError(errorMsg);
                    }
                    logger.warning("Backend server is not available");
                } else {
                    logger.info("Successfully connected to backend server");
                }
            });
        });
    }

    /**
     * Analyzes the provided code using the backend API and updates the UI with results.
     * @param code The source code to analyze
     */
    public void analyzeCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            if (modernView != null) {
                modernView.displayError("No code to analyze");
            } else if (mainView != null) {
                mainView.displayError("No code to analyze");
            }
            return;
        }
        
        // Show loading state
        Platform.runLater(() -> {
            if (modernView != null) {
                modernView.displayError("Analyzing code...");
            } else if (mainView != null) {
                mainView.displayError("Analyzing code...");
            }
        });
        
        // Use the API client to analyze code
        apiClientService.analyzeCode(code).whenComplete((result, ex) -> {
            Platform.runLater(() -> {
                if (ex != null) {
                    String errorMsg = "Analysis failed: " + ex.getMessage();
                    if (modernView != null) {
                        modernView.displayError(errorMsg);
                    } else if (mainView != null) {
                        mainView.displayError(errorMsg);
                    }
                    logger.log(Level.SEVERE, "Code analysis failed", ex);
                } else {
                    // Store the result for later use
                    lastResult = result;
                    
                    if (modernView != null) {
                        modernView.displayAnalysisResult(result);
                    } else if (mainView != null) {
                        mainView.displayAnalysisResult(result);
                    }
                    
                    // Store analysis record for trend tracking
                    if (!currentFileName.isEmpty()) {
                        storeAnalysisRecord(result);
                    }
                }
            });
        });
    }

    public void onOpenFile(Stage primaryStage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Java File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Java Files", "*.java"));
        File file = fileChooser.showOpenDialog(primaryStage);
        if (file != null) {
            try {
                String content = Files.readString(file.toPath());
                currentFileName = file.getName();
                mainView.displayCode(content);
                analyzeCode(content);
            } catch (Exception ex) {
                mainView.displayError("Failed to load file: " + ex.getMessage());
            }
        }
    }

    public void analyzeAndDisplay(String code) {
        // This method now delegates to the main analyzeCode method
        // which uses the API client
        analyzeCode(code);
    }
    
    private void storeAnalysisRecord(CodeAnalysisResult result) {
        try {
            // Create a simple analysis record for tracking
            String record = String.format(
                "{\"timestamp\":\"%s\", \"fileName\":\"%s\", \"totalIssues\":%d, \"errors\":%d, \"suggestions\":%d}",
                java.time.Instant.now().toString(),
                currentFileName,
                result.getTotalIssueCount(),
                result.getErrors().size(),
                result.getSuggestions().size()
            );
            
            // Append to analysis history file
            String homeDir = System.getProperty("user.home");
            String historyFile = homeDir + "/analysis_history.json";
            
            try (FileWriter writer = new FileWriter(historyFile, true)) {
                writer.write(record + "\n");
            }
        } catch (IOException e) {
            // Silently fail - trend tracking is not critical
            System.err.println("Failed to store analysis record: " + e.getMessage());
        }
    }
    
    /**
     * Handle the Ask AI button click (this method is no longer directly used - see onAskAiWithQuestion)
     * @deprecated Use onAskAiWithQuestion instead
     */
    @Deprecated
    public void onAskAi() {
        // Default question if none provided
        onAskAiWithQuestion(mainView.getCurrentCode(), "Please review this code and suggest improvements.");
    }
    
    /**
     * Handle AI request with a specific question using the backend API
     * @param code The code to analyze
     * @param question The user's question about the code
     */
    public void onAskAiWithQuestion(String code, String question) {
        if (code == null || code.trim().isEmpty()) {
            mainView.displayError("No code to analyze");
            return;
        }
        
        mainView.showAiLoading();
        mainView.clearAiSuggestion();
        
        logger.info("Starting AI request with question: \"" + question + "\"");
        
        // Show some immediate feedback
        Platform.runLater(() -> mainView.appendAiSuggestion("Processing your question: \"" + question + "\"\n\n"));
        
        logger.info("Using AI service from backend server");
        
        // Use the API client for AI suggestions with streaming
        CompletableFuture<Void> future = apiClientService.getCodeSuggestionStreaming(
                code, 
                question,
                // This consumer will be called for each chunk of the response
                chunk -> Platform.runLater(() -> mainView.appendAiSuggestion(chunk))
        );
        
        // Handle completion
        future.exceptionally(ex -> {
            Platform.runLater(() -> {
                logger.log(Level.SEVERE, "AI suggestion error", ex);
                mainView.appendAiSuggestion("\n\nError getting AI suggestion: " + ex.getMessage());
                mainView.appendAiSuggestion("\n\nTroubleshooting steps:\n" +
                        "1. Check that the backend server is running\n" +
                        "2. Verify your AI service configuration\n" +
                        "3. Check your internet connection\n" +
                        "4. Try with a simpler question");
                mainView.hideAiLoading();
            });
            return null;
        }).thenRun(() -> Platform.runLater(() -> {
            mainView.hideAiLoading();
        }));
    }

    public CodeAnalysisResult getLastResult() {
        return lastResult;
    }
} 