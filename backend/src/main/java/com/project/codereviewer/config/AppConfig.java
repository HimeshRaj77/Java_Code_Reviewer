package com.project.codereviewer.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class AppConfig {
    private static final Logger LOGGER = Logger.getLogger(AppConfig.class.getName());
    
    private static final String CONFIG_FILE = "application.properties";
    private static final String OPENROUTER_API_KEY_PROP = "openrouter.api.key";
    private static final String OPENROUTER_MODEL_NAME_PROP = "openrouter.model.name";
    
    private static volatile Properties config;

    private AppConfig() {
        throw new AssertionError("AppConfig is a utility class and should not be instantiated.");
    }

    public static String getOpenRouterApiKey() throws IOException {
        Properties props = getConfig();
        String apiKey = props.getProperty(OPENROUTER_API_KEY_PROP);
        if (apiKey == null || apiKey.isBlank() || apiKey.equals("YOUR_API_KEY_HERE")) {
            throw new IOException("OpenRouter API key not found or not set in application.properties file. Please add 'openrouter.api.key=YOUR_API_KEY' to your configuration.");
        }
        return apiKey;
    }

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