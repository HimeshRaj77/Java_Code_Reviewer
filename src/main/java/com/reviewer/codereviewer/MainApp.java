package com.reviewer.codereviewer;

import com.reviewer.codereviewer.controller.MainController;
import javafx.application.Application;
import javafx.stage.Stage;
import java.nio.file.Files;
import java.nio.file.Paths;

public class MainApp extends Application {
    @Override
    public void start(Stage primaryStage) {
        MainController controller = new MainController();
        controller.start(primaryStage);
    }

    public static void main(String[] args) {
        launch(args);
    }
} 