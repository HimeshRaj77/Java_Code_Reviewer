package com.project.codereviewer.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class SecurityUtils {
    private static final Logger logger = Logger.getLogger(SecurityUtils.class.getName());
    private static final SecureRandom secureRandom = new SecureRandom();
    
    private static final Pattern API_KEY_PATTERN = Pattern.compile("^[a-zA-Z0-9_-]{20,}$");
    private static final Pattern SAFE_FILENAME_PATTERN = Pattern.compile("^[a-zA-Z0-9._-]+\\.java$");
    private static final Pattern MODEL_NAME_PATTERN = Pattern.compile("^[a-zA-Z0-9/._-]+$");
    
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;
    
    public static boolean isValidApiKey(String apiKey) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            return false;
        }
        
        if ("YOUR_API_KEY_HERE".equals(apiKey) || 
            "sk-your-key-here".equals(apiKey) ||
            "replace-with-your-key".equals(apiKey)) {
            return false;
        }
        
        return API_KEY_PATTERN.matcher(apiKey.trim()).matches();
    }
    
    public static String maskApiKey(String apiKey) {
        if (apiKey == null || apiKey.length() < 8) {
            return "***";
        }
        
        int visibleChars = Math.min(4, apiKey.length() / 4);
        String prefix = apiKey.substring(0, visibleChars);
        String suffix = apiKey.substring(apiKey.length() - visibleChars);
        
        return prefix + "***" + suffix;
    }
    
    public static boolean isSafeFilename(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            return false;
        }
        
        String normalizedName = filename.trim();
        
        if (normalizedName.contains("..") || 
            normalizedName.contains("/") || 
            normalizedName.contains("\\")) {
            return false;
        }
        
        return SAFE_FILENAME_PATTERN.matcher(normalizedName).matches();
    }
    
    public static boolean isSafeModelName(String modelName) {
        if (modelName == null || modelName.trim().isEmpty()) {
            return false;
        }
        
        return MODEL_NAME_PATTERN.matcher(modelName.trim()).matches();
    }
    
    public static String sanitizeInput(String input) {
        if (input == null) {
            return "";
        }
        
        return input.replaceAll("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F\\x7F]", "")
                   .trim();
    }
    
    public static boolean isValidFileSize(long fileSize) {
        return fileSize > 0 && fileSize <= MAX_FILE_SIZE;
    }
    
    public static String generateCacheKey(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            logger.warning("SHA-256 not available, using fallback hash");
            return String.valueOf(input.hashCode());
        }
    }
    
    public static String generateSessionId() {
        byte[] randomBytes = new byte[16];
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }
    
    public static boolean isValidApiUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return false;
        }
        
        String normalizedUrl = url.trim().toLowerCase();
        return normalizedUrl.startsWith("https://openrouter.ai/") ||
               normalizedUrl.startsWith("https://api.openrouter.ai/");
    }
    
    public static class ValidationResult {
        private final boolean valid;
        private final String message;
        
        public ValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }
        
        public boolean isValid() { return valid; }
        public String getMessage() { return message; }
        
        public static ValidationResult success() {
            return new ValidationResult(true, "Validation successful");
        }
        
        public static ValidationResult failure(String message) {
            return new ValidationResult(false, message);
        }
    }
    
    public static ValidationResult validateApiRequest(String apiKey, String modelName, String content) {
        if (!isValidApiKey(apiKey)) {
            return ValidationResult.failure("Invalid API key format");
        }
        
        if (!isSafeModelName(modelName)) {
            return ValidationResult.failure("Invalid model name");
        }
        
        if (content == null || content.trim().isEmpty()) {
            return ValidationResult.failure("Content cannot be empty");
        }
        
        if (content.length() > MAX_FILE_SIZE) {
            return ValidationResult.failure("Content too large");
        }
        
        return ValidationResult.success();
    }
}
