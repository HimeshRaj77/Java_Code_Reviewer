package com.project.codereviewer.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Centralized configuration class for handling application settings and secrets.
 * It loads API keys from a 'secrets.properties' file which should be excluded from version control.
 */
public final class AppConfig {
    private static final Logger LOGGER = Logger.getLogger(AppConfig.class.getName());
    
    private static final String CONFIG_FILE = "application.properties";
    private static final String OPENROUTER_API_KEY_PROP = "openrouter.api.key";
    private static final String OPENROUTER_MODEL_NAME_PROP = "openrouter.model.name";
    
    private static volatile Properties config;

    // Private constructor to prevent instantiation of this utility class.
    private AppConfig() {
        throw new AssertionError("AppConfig is a utility class and should not be instantiated.");
    }

    /**
     * Get the OpenRouter API key from the secrets.properties file.
     * * @return The API key as a String.
     * @throws IOException If the API key cannot be retrieved.
     */
    public static String getOpenRouterApiKey() throws IOException {
        Properties props = getConfig();
        String apiKey = props.getProperty(OPENROUTER_API_KEY_PROP);
        if (apiKey == null || apiKey.isBlank() || apiKey.equals("YOUR_API_KEY_HERE")) {
            throw new IOException("OpenRouter API key not found or not set in application.properties file. Please add 'openrouter.api.key=YOUR_API_KEY' to your configuration.");
        }
        return apiKey;
    }

    /**
     * Get the OpenRouter model name from the application.properties file, or use a default.
     * * @param defaultModel The default model to use if one is not specified in the properties file.
     * @return The model name as a String.
     */
    public static String getOpenRouterModelName(String defaultModel) {
        try {
            Properties props = getConfig();
            String model = props.getProperty(OPENROUTER_MODEL_NAME_PROP);
            
            if (model != null && !model.isBlank()) {
                LOGGER.log(Level.INFO, "Using AI model from configuration: {0}", model);
                return model;
            } else {
                LOGGER.log(Level.WARNING, "Model not specified in configuration. Using default model: {0}", defaultModel);
                return defaultModel;
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Could not load secrets.properties to check for a model name. Using default: {0}", defaultModel);
            return defaultModel;
        }
    }

    /**
     * Loads secrets from the properties file using a synchronized, double-checked locking pattern.
     * It searches for the 'secrets.properties' file in the classpath, current directory, and user's home directory.
     * * @return A Properties object containing the secrets.
     * @throws IOException If the secrets file cannot be found or loaded.
     */
    private static Properties getConfig() throws IOException {
        if (config == null) {
            synchronized (AppConfig.class) {
                if (config == null) {
                    config = loadProperties();
                }
            }
        }
        return config;
    }
    
    private static Properties loadProperties() throws IOException {
        Properties props = new Properties();
        
        // Try loading from classpath
        try (InputStream in = AppConfig.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (in != null) {
                LOGGER.info("Found application.properties in classpath.");
                props.load(in);
                return props;
            }
        }
        
        // Try loading from current directory
        Path configPath = Paths.get(CONFIG_FILE);
        if (Files.exists(configPath)) {
            LOGGER.info("Found application.properties in current directory: " + configPath.toAbsolutePath());
            try (InputStream in = Files.newInputStream(configPath)) {
                props.load(in);
                return props;
            }
        }

        // Try loading from user home directory
        Path homeConfigPath = Paths.get(System.getProperty("user.home"), CONFIG_FILE);
        if (Files.exists(homeConfigPath)) {
            LOGGER.info("Found application.properties in user home directory: " + homeConfigPath.toAbsolutePath());
            try (InputStream in = Files.newInputStream(homeConfigPath)) {
                props.load(in);
                return props;
            }
        }

        throw new IOException("application.properties file not found. Please create it in the classpath or current directory.");
    }
}