package com.reviewer.codereviewer.controller;

import com.reviewer.codereviewer.model.CodeAnalysisResult;
import com.reviewer.codereviewer.model.CodeIssue;
import com.reviewer.codereviewer.model.AnalysisRecord;
import com.reviewer.codereviewer.repo.JavaFileRepository;
import com.reviewer.codereviewer.service.AISuggestionService;
import com.reviewer.codereviewer.service.CodeAnalysisService;
import com.reviewer.codereviewer.ui.MainView;
import javafx.application.Platform;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainController {
    private static final Logger logger = Logger.getLogger(MainController.class.getName());
    
    private MainView mainView;
    private final JavaFileRepository fileRepo = new JavaFileRepository();
    private final CodeAnalysisService analysisService = new CodeAnalysisService();
    private final AISuggestionService aiService = new AISuggestionService();
    
    /**
     * Get the AI suggestion service
     * @return the AI service
     */
    public AISuggestionService getAiService() {
        return aiService;
    }
    private CodeAnalysisResult lastResult;
    private String currentFileName = "";

    public void start(Stage primaryStage) {
        mainView = new MainView(this);
        mainView.start(primaryStage);
    }

    public void onOpenFile(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Java File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Java Files", "*.java"));
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            try {
                String content = fileRepo.loadFile(file);
                currentFileName = file.getName();
                mainView.displayCode(content);
                analyzeAndDisplay(content);
            } catch (Exception ex) {
                mainView.displayError("Failed to load file: " + ex.getMessage());
            }
        }
    }

    public void analyzeAndDisplay(String code) {
        lastResult = analysisService.analyze(code);
        mainView.displayAnalysisResult(lastResult);
        
        // Store analysis record for trend tracking
        if (!currentFileName.isEmpty()) {
            storeAnalysisRecord(lastResult);
        }
    }
    
    private void storeAnalysisRecord(CodeAnalysisResult result) {
        try {
            AnalysisRecord record = new AnalysisRecord(currentFileName);
            record.setTotalIssues(result.getErrors().size() + result.getSuggestions().size());
            record.setTotalErrors(result.getErrors().size());
            record.setTotalSuggestions(result.getSuggestions().size());
            
            // Extract complexity metrics from suggestions
            for (CodeIssue issue : result.getSuggestions()) {
                if (issue.getMessage().contains("Cyclomatic complexity:")) {
                    String methodName = issue.getMessage().split(":")[0];
                    String complexityStr = issue.getMessage().split(":")[2].trim();
                    try {
                        int complexity = Integer.parseInt(complexityStr);
                        record.addComplexityMetric(methodName, complexity);
                    } catch (NumberFormatException e) {
                        // Ignore parsing errors
                    }
                }
            }
            
            // Append to analysis history file
            String homeDir = System.getProperty("user.home");
            String historyFile = homeDir + "/analysis_history.json";
            
            try (FileWriter writer = new FileWriter(historyFile, true)) {
                writer.write(record.toString() + "\n");
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
     * Handle AI request with a specific question
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
        
        // Add the question to the code with a more focused prompt
        String promptWithQuestion;
        
        if (question.toLowerCase().contains("review") || question.toLowerCase().contains("analyze")) {
            // For review-type questions, use a more structured format
            promptWithQuestion = "CODE:\n```java\n" + code + "\n```\n\n" + 
                               "USER REQUEST: " + question + "\n\n" +
                               "Please provide a clear and specific response focusing on the user's request. " +
                               "If this is a code review request, focus on the most important issues first.";
        } else {
            // For specific questions, use a simpler format
            promptWithQuestion = "CODE:\n```java\n" + code + "\n```\n\n" + 
                               "USER QUESTION: " + question + "\n\n" +
                               "Please answer this specific question about the code above.";
        }
        
        logger.info("Using model from configuration");
        
        // Use streaming API for better user experience
        CompletableFuture<Void> future = aiService.getCodeSuggestionStreaming(
                promptWithQuestion, 
                // This consumer will be called for each chunk of the response
                chunk -> Platform.runLater(() -> mainView.appendAiSuggestion(chunk))
        );
        
        // Handle completion
        future.exceptionally(ex -> {
            Platform.runLater(() -> {
                logger.log(Level.SEVERE, "AI suggestion error", ex);
                mainView.appendAiSuggestion("\n\nError getting AI suggestion: " + ex.getMessage());
                mainView.hideAiLoading();
            });
            return null;
        }).thenRun(() -> Platform.runLater(mainView::hideAiLoading));
    }

    public CodeAnalysisResult getLastResult() {
        return lastResult;
    }
} 