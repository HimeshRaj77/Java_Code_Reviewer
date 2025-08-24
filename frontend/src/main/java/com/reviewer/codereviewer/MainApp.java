package com.reviewer.codereviewer;

import com.reviewer.codereviewer.controller.MainController;
import com.reviewer.codereviewer.ui.ModernMainView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.logging.Logger;

public class MainApp extends Application {
    private static final Logger logger = Logger.getLogger(MainApp.class.getName());
    
    @Override
    public void start(Stage primaryStage) {
        try {
            logger.info("Starting CodeReviewer Frontend Application");
            
            // Create main controller
            MainController controller = new MainController();
            
            // Create and configure the modern view
            ModernMainView modernView = new ModernMainView(controller);
            controller.setModernView(modernView);
            
            // Create and show the scene
            Scene scene = modernView.createScene();
            primaryStage.setTitle("CodeReviewer - Frontend Client");
            primaryStage.setScene(scene);
            primaryStage.setResizable(true);
            primaryStage.setMaximized(true);
            primaryStage.show();
            
            logger.info("Application started successfully");
            
            // Show a status message about backend connection
            modernView.displayError("Frontend application started. Connecting to backend server...");
            
        } catch (Exception e) {
            logger.severe("Application startup failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            logger.info("Launching CodeReviewer Frontend Client");
            launch(args);
        } catch (Exception e) {
            logger.severe("Application startup failed: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
} 