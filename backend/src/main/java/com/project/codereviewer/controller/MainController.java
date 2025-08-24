package com.project.codereviewer.controller;

import com.project.codereviewer.model.CodeIssue;
import com.project.codereviewer.model.CodeAnalysisResult;
import com.project.codereviewer.model.AnalysisRecord;
import com.project.codereviewer.model.CommonQuickFixes;
import com.project.codereviewer.service.AISuggestionService;
import com.project.codereviewer.service.CodeAnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/code-review")
@CrossOrigin(origins = "*")
public class MainController {
    private static final Logger logger = Logger.getLogger(MainController.class.getName());
    
    @Autowired
    private CodeAnalysisService analysisService;
    
    @Autowired
    private AISuggestionService aiService;
    
    private String currentFileName = "";

    /**
     * Health check endpoint
     * @return Simple status message
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Code Reviewer Backend is running!");
    }

    /**
     * Analyzes the provided code and returns results.
     * @param request The code analysis request containing the code to analyze
     * @return CodeAnalysisResult with analysis findings
     */
    @PostMapping("/analyze")
    public ResponseEntity<CodeAnalysisResult> analyzeCode(@RequestBody CodeAnalysisRequest request) {
        if (request.getCode() == null || request.getCode().trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        try {
            CodeAnalysisResult result = analysisService.analyze(request.getCode());
            
            // Add quick fixes to applicable issues
            addQuickFixes(result);
            
            // Store analysis record for trend tracking
            if (request.getFileName() != null && !request.getFileName().isEmpty()) {
                currentFileName = request.getFileName();
                storeAnalysisRecord(result);
            }
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Analysis failed", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Uploads and analyzes a Java file
     * @param file The Java file to analyze
     * @return CodeAnalysisResult with analysis findings
     */
    @PostMapping("/upload")
    public ResponseEntity<CodeAnalysisResult> uploadAndAnalyze(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        try {
            String content = new String(file.getBytes(), StandardCharsets.UTF_8);
            currentFileName = file.getOriginalFilename();
            
            CodeAnalysisResult result = analysisService.analyze(content);
            addQuickFixes(result);
            storeAnalysisRecord(result);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "File analysis failed", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get AI suggestions for code with a specific question
     * @param request The AI suggestion request
     * @return AI response
     */
    @PostMapping("/ai-suggest")
    public ResponseEntity<AISuggestionResponse> getAISuggestion(@RequestBody AISuggestionRequest request) {
        if (request.getCode() == null || request.getCode().trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        try {
            String question = request.getQuestion() != null ? request.getQuestion() : 
                            "Please review this code and suggest improvements.";
            
            logger.info("Starting AI request with question: \"" + question + "\"");
            
            String promptWithQuestion = buildPrompt(request.getCode(), question);
            
            // Get synchronous response
            String suggestion = aiService.getCodeSuggestion(promptWithQuestion);
            
            return ResponseEntity.ok(new AISuggestionResponse(suggestion));
        } catch (Exception e) {
            logger.log(Level.SEVERE, "AI suggestion failed", e);
            return ResponseEntity.ok(new AISuggestionResponse("Error getting AI suggestion: " + e.getMessage()));
        }
    }

    private void addQuickFixes(CodeAnalysisResult result) {
        for (CodeIssue issue : result.getErrors()) {
            if (issue.getMessage().toLowerCase().contains("unused import")) {
                issue.addQuickFix(CommonQuickFixes.unusedImportFix());
            } else if (issue.getMessage().toLowerCase().contains("magic number")) {
                issue.addQuickFix(CommonQuickFixes.magicNumberFix());
            } else if (issue.getMessage().toLowerCase().contains("method is too long")) {
                issue.addQuickFix(CommonQuickFixes.longMethodFix());
            } else if (issue.getMessage().toLowerCase().contains("empty catch block")) {
                issue.addQuickFix(CommonQuickFixes.emptyCatchBlockFix());
            } else if (issue.getMessage().toLowerCase().contains("poor variable name")) {
                issue.addQuickFix(CommonQuickFixes.poorVariableNameFix());
            }
        }
    }

    private String buildPrompt(String code, String question) {
        if (question.toLowerCase().contains("review") || question.toLowerCase().contains("analyze")) {
            return "CODE:\n```java\n" + code + "\n```\n\n" + 
                   "USER REQUEST: " + question + "\n\n" +
                   "Please provide a clear and specific response focusing on the user's request. " +
                   "If this is a code review request, focus on the most important issues first.";
        } else {
            return "CODE:\n```java\n" + code + "\n```\n\n" + 
                   "USER QUESTION: " + question + "\n\n" +
                   "Please answer this specific question about the code above.";
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

    // Request/Response DTOs
    public static class CodeAnalysisRequest {
        private String code;
        private String fileName;
        
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        public String getFileName() { return fileName; }
        public void setFileName(String fileName) { this.fileName = fileName; }
    }
    
    public static class AISuggestionRequest {
        private String code;
        private String question;
        
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        public String getQuestion() { return question; }
        public void setQuestion(String question) { this.question = question; }
    }
    
    public static class AISuggestionResponse {
        private String suggestion;
        
        public AISuggestionResponse(String suggestion) {
            this.suggestion = suggestion;
        }
        
        public String getSuggestion() { return suggestion; }
        public void setSuggestion(String suggestion) { this.suggestion = suggestion; }
    }
} 