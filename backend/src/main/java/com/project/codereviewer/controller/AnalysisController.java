package com.project.codereviewer.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import com.project.codereviewer.model.CodeAnalysisResult;
import com.project.codereviewer.service.CodeAnalysisService;
import com.project.codereviewer.service.AISuggestionService;
import java.util.logging.Logger;

/**
 * REST Controller for handling code analysis requests.
 * Acts as a switchboard operator that listens for incoming HTTP requests
 * and directs them to the appropriate service methods.
 */
@RestController
@RequestMapping("/api/analysis")
public class AnalysisController {
    
    private static final Logger logger = Logger.getLogger(AnalysisController.class.getName());
    
    /**
     * Dependency Injection: Instead of creating service instances ourselves,
     * we ask Spring framework to provide ready-to-use instances.
     * This applies Inversion of Control (IoC) principle.
     */
    @Autowired
    private CodeAnalysisService analysisService;
    
    @Autowired
    private AISuggestionService aiService;
    
    /**
     * Health check endpoint - like checking if the phone line is working
     * @return Simple status message
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Analysis service is running!");
    }
    
    /**
     * Static code analysis endpoint - the main "phone extension" for code analysis
     * This method handles requests to analyze Java source code statically
     * 
     * @param code The Java source code to analyze (sent in request body)
     * @return CodeAnalysisResult containing errors, suggestions, and metrics
     */
    @PostMapping("/static")
    public ResponseEntity<CodeAnalysisResult> analyzeStatic(@RequestBody String code) {
        logger.info("Received static analysis request for code of length: " + code.length());
        
        try {
            // Use the injected service to perform analysis
            CodeAnalysisResult result = analysisService.analyze(code);
            logger.info("Analysis completed. Found " + result.getErrors().size() + 
                       " errors and " + result.getSuggestions().size() + " suggestions");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.severe("Analysis failed: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * AI-powered code analysis endpoint - another "phone extension" for AI review
     * This method handles requests for AI-powered code suggestions
     * 
     * @param code The Java source code to get AI suggestions for
     * @return AI suggestions and recommendations
     */
    @PostMapping("/ai")
    public ResponseEntity<String> analyzeWithAI(@RequestBody String code) {
        logger.info("Received AI analysis request for code of length: " + code.length());
        
        try {
            // Use the injected AI service to get suggestions
            String suggestion = aiService.getCodeSuggestion(code);
            logger.info("AI analysis completed successfully");
            return ResponseEntity.ok(suggestion);
        } catch (Exception e) {
            logger.severe("AI analysis failed: " + e.getMessage());
            return ResponseEntity.ok("Error getting AI suggestion: " + e.getMessage());
        }
    }
    
}
