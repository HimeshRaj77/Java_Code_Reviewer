package com.reviewer.codereviewer.ui;

import com.reviewer.codereviewer.controller.MainController;
import com.reviewer.codereviewer.model.CodeAnalysisResult;
import com.reviewer.codereviewer.model.CodeIssue;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.scene.control.SplitPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import java.io.FileWriter;
import java.io.IOException;
import java.io.File;

import javafx.scene.control.Tooltip;
import javafx.util.Duration;
import java.util.HashMap;
import java.util.Map;
import javafx.scene.layout.StackPane;
import javafx.geometry.Insets;
import javafx.scene.text.Text;
import javafx.scene.shape.Rectangle;
import java.util.concurrent.CompletableFuture;

public class MainView {
    private final MainController controller;
    private CodeArea codeArea;
    private ListView<CodeIssue> errorListView;
    private ListView<CodeIssue> suggestionListView;
    private ObservableList<CodeIssue> errors;
    private ObservableList<CodeIssue> suggestions;
    private Stage primaryStage;
    private Label detailsHeader;
    private TextArea detailsArea;
    private Map<Integer, String> errorTooltips = new HashMap<>();
    private VBox heatmapPane;
    
    // AI review components
    private TextArea aiSuggestionArea;
    private SplitPane codeSplitPane;
    private ProgressIndicator aiProgressIndicator;

    public MainView(MainController controller) {
        this.controller = controller;
    }

    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("Java Code Reviewer");
        
        // Set minimum window size for usability
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);

        BorderPane root = new BorderPane();
        root.setPrefSize(1024, 768);
        
        codeArea = new CodeArea();
        codeArea.setEditable(false);
        codeArea.setWrapText(false);
        // Do not set paragraph graphic factory here; set it only when code is loaded
        codeArea.getStyleClass().add("code-area");

        // Create heatmap pane with better sizing
        heatmapPane = new VBox();
        heatmapPane.setPrefWidth(25);
        heatmapPane.setMinWidth(25);
        heatmapPane.setMaxWidth(25);
        heatmapPane.getStyleClass().add("heatmap-pane");
        
        // Set proper growth behavior for code area
        codeArea.setMinWidth(400);
        codeArea.setPrefWidth(3000);
        HBox.setHgrow(codeArea, javafx.scene.layout.Priority.ALWAYS);
        
        // Create HBox to hold code area and heatmap with proper spacing
        HBox codeAndHeatmap = new HBox();
        codeAndHeatmap.getChildren().addAll(codeArea, heatmapPane);
        codeAndHeatmap.setSpacing(5);
        codeAndHeatmap.getStyleClass().add("code-heatmap-container");
        HBox.setHgrow(codeAndHeatmap, javafx.scene.layout.Priority.ALWAYS);
        
        // Initialize AI suggestion area
        aiSuggestionArea = new TextArea();
        aiSuggestionArea.setEditable(false);
        aiSuggestionArea.setWrapText(true);
        aiSuggestionArea.setPrefRowCount(10);
        aiSuggestionArea.setVisible(false);
        aiSuggestionArea.getStyleClass().add("ai-suggestion-area");
        
        // Progress indicator for AI review
        aiProgressIndicator = new ProgressIndicator();
        aiProgressIndicator.setMaxSize(20, 20);
        aiProgressIndicator.setVisible(false);

        // Create button for AI review
        Button reviewButton = new Button("🤖 AI Code Review");
        reviewButton.getStyleClass().add("ai-button");
        reviewButton.setOnAction(e -> requestAiReview());
        
        HBox aiButtonBox = new HBox(5, reviewButton, aiProgressIndicator);
        aiButtonBox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        aiButtonBox.setPadding(new Insets(5));
        
        // Create vertical split for code and AI area with proper resize behavior
        codeSplitPane = new SplitPane();
        codeSplitPane.setOrientation(javafx.geometry.Orientation.VERTICAL);
        VBox.setVgrow(codeSplitPane, javafx.scene.layout.Priority.ALWAYS);
        
        // Add a VBox to properly manage the code area and AI suggestion area
        VBox codeContainer = new VBox();
        codeContainer.getChildren().addAll(aiButtonBox, codeAndHeatmap);
        VBox.setVgrow(codeAndHeatmap, javafx.scene.layout.Priority.ALWAYS);
        
        // Setup AI suggestion area with proper resize behavior
        VBox aiContainer = new VBox();
        aiContainer.getChildren().add(aiSuggestionArea);
        VBox.setVgrow(aiSuggestionArea, javafx.scene.layout.Priority.ALWAYS);
        
        codeSplitPane.getItems().addAll(codeContainer, aiContainer);
        codeSplitPane.setDividerPositions(0.7);
        codeSplitPane.getStyleClass().add("code-split-pane");

        // Initialize collections and lists with better sizing
        errors = FXCollections.observableArrayList();
        suggestions = FXCollections.observableArrayList();
        errorListView = new ListView<>(errors);
        suggestionListView = new ListView<>(suggestions);
        
        // Better proportional sizing for lists
        errorListView.setPrefHeight(200);
        errorListView.setMinHeight(150);
        suggestionListView.setPrefHeight(250);
        suggestionListView.setMinHeight(200);
        
        errorListView.setCellFactory(lv -> createStyledCell());
        suggestionListView.setCellFactory(lv -> createStyledCell());
        errorListView.setOnMouseClicked(this::onIssueClicked);
        suggestionListView.setOnMouseClicked(this::onIssueClicked);
        errorListView.getStyleClass().add("list-view");
        suggestionListView.getStyleClass().add("list-view");

        // Enhanced headers with icons
        Label errorHeader = new Label("🚨 Critical Issues");
        errorHeader.getStyleClass().add("section-header");
        errorHeader.getStyleClass().add("error-header");
        
        Label suggestionHeader = new Label("💡 Suggestions");
        suggestionHeader.getStyleClass().add("section-header");
        suggestionHeader.getStyleClass().add("suggestion-header");

        detailsHeader = new Label("📋 Issue Details");
        detailsHeader.getStyleClass().add("section-header");
        detailsHeader.getStyleClass().add("details-header");
        
        detailsArea = new TextArea();
        detailsArea.setEditable(false);
        detailsArea.setWrapText(true);
        detailsArea.setPrefRowCount(8);
        detailsArea.setMinHeight(120);
        detailsArea.getStyleClass().add("details-area");

        // Improved right pane layout with better spacing
        VBox rightPane = new VBox();
        rightPane.setSpacing(15);
        rightPane.setPadding(new Insets(15));
        rightPane.getChildren().addAll(
            errorHeader, errorListView, 
            suggestionHeader, suggestionListView, 
            detailsHeader, detailsArea
        );
        rightPane.getStyleClass().add("right-pane");
        rightPane.setMaxWidth(400);
        rightPane.setPrefWidth(350);
        rightPane.setMinWidth(300);

        // Main split pane with better proportions
        SplitPane mainSplitPane = new SplitPane();
        mainSplitPane.getItems().addAll(codeSplitPane, rightPane);
        mainSplitPane.setDividerPositions(0.7);
        mainSplitPane.getStyleClass().add("main-split-pane");
        
        // Set proper growth behavior for the code area side of the split pane
        codeSplitPane.setMinWidth(500);
        codeSplitPane.setPrefWidth(1000);
        SplitPane.setResizableWithParent(codeSplitPane, true);
        SplitPane.setResizableWithParent(rightPane, false);

        MenuBar menuBar = new MenuBar();
        Menu fileMenu = new Menu("File");
        // Create menu items with icons
        MenuItem openItem = new MenuItem("Open Java File...");
        FontIcon openIcon = new FontIcon(FontAwesomeSolid.FOLDER_OPEN);
        openIcon.setIconSize(16);
        openItem.setGraphic(openIcon);
        openItem.setOnAction(e -> controller.onOpenFile(primaryStage));
        MenuItem exportItem = new MenuItem("Export Analysis as Markdown");
        FontIcon exportIcon = new FontIcon(FontAwesomeSolid.DOWNLOAD);
        exportIcon.setIconSize(16);
        exportItem.setGraphic(exportIcon);
        exportItem.setOnAction(e -> exportAnalysisAsMarkdown());
        fileMenu.getItems().addAll(openItem, exportItem);
        
        // Create AI menu
        Menu aiMenu = new Menu("AI");
        MenuItem toggleAiItem = new MenuItem("Toggle AI Suggestion Panel");
        FontIcon aiIcon = new FontIcon(FontAwesomeSolid.ROBOT);
        aiIcon.setIconSize(16);
        toggleAiItem.setGraphic(aiIcon);
        toggleAiItem.setOnAction(e -> toggleAiSuggestionPanel());
        aiMenu.getItems().add(toggleAiItem);
        
        menuBar.getMenus().addAll(fileMenu, aiMenu);
        menuBar.getStyleClass().add("menu-bar");

        // Add the AI button to the code area header
        HBox codeHeader = new HBox();
        codeHeader.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        codeHeader.getChildren().add(aiButtonBox);
        codeHeader.setPadding(new Insets(5, 10, 5, 10));
        codeHeader.setSpacing(10);
        
        VBox codeContainerWithHeader = new VBox(codeHeader, mainSplitPane);
        VBox.setVgrow(mainSplitPane, javafx.scene.layout.Priority.ALWAYS);
        
        root.setTop(menuBar);
        root.setCenter(codeContainerWithHeader);

        Scene scene = new Scene(root, 1400, 900);
        scene.getStylesheets().add(getClass().getResource("/app-theme.css").toExternalForm());
        scene.getStylesheets().add(getClass().getResource("/ai-style.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(1200);
        primaryStage.setMinHeight(800);
        primaryStage.show();
    }

    public void displayCode(String code) {
        codeArea.replaceText(code);
        codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));
        clearAllLineHighlights();
        onCodeLoaded();
    }

    public void displayError(String message) {
        errors.clear();
        suggestions.clear();
    }

    public void clearCodeArea() {
        codeArea.clear();
        codeArea.setParagraphGraphicFactory(null);
    }

    public void displayAnalysisResult(CodeAnalysisResult result) {
        errors.setAll(result.getErrors()); // Show errors in the list
        suggestions.setAll(result.getSuggestions());
        clearAllLineHighlights();
        errorTooltips.clear();
        for (CodeIssue issue : result.getErrors()) {
            String styleClass = getHighlightClass(issue);
            highlightLine(issue.getLineNumber(), styleClass);
            errorTooltips.put(issue.getLineNumber(), issue.getMessage());
        }
        // Set paragraph graphic factory for tooltips
        codeArea.setParagraphGraphicFactory(line -> {
            int lineNum = line + 1;
            Text lineNo = new Text(String.valueOf(lineNum));
            StackPane graphic = new StackPane(lineNo);
            graphic.getStyleClass().add("lineno-graphic");
            if (errorTooltips.containsKey(lineNum)) {
                Tooltip tooltip = new Tooltip(errorTooltips.get(lineNum));
                tooltip.setShowDelay(Duration.millis(100));
                Tooltip.install(graphic, tooltip);
                // Add severity class for color
                String severityClass = "";
                String msg = errorTooltips.get(lineNum).toLowerCase();
                if (msg.contains("critical")) severityClass = "critical-error-highlight";
                else if (msg.contains("warning")) severityClass = "warning-highlight";
                else if (msg.contains("info")) severityClass = "info-highlight";
                if (!severityClass.isEmpty()) graphic.getStyleClass().add(severityClass);
            }
            return graphic;
        });
        for (CodeIssue issue : suggestions) {
            if (issue.getType() == CodeIssue.Type.SUGGESTION) {
                highlightLine(issue.getLineNumber(), "suggestion-highlight");
            }
        }
        
        // Render heatmap
        renderHeatmap(result);
    }

    private String getHighlightClass(CodeIssue issue) {
        if ("critical".equalsIgnoreCase(issue.getSeverity())) {
            return "critical-error-highlight";
        } else if ("warning".equalsIgnoreCase(issue.getSeverity())) {
            return "warning-highlight";
        } else if ("info".equalsIgnoreCase(issue.getSeverity())) {
            return "info-highlight";
        } else {
            return "long-method-highlight";
        }
    }

    private ListCell<CodeIssue> createStyledCell() {
        return new ListCell<>() {
            @Override
            protected void updateItem(CodeIssue item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                    setTooltip(null);
                    setGraphic(null);
                } else {
                    // Create icon based on issue type and severity
                    FontIcon icon = new FontIcon();
                    icon.setIconSize(14);
                    
                    if (item.getType() == CodeIssue.Type.ERROR) {
                        if ("critical".equalsIgnoreCase(item.getSeverity())) {
                            icon.setIconLiteral("fas-exclamation-triangle");
                            icon.setStyle("-fx-text-fill: #ff4444;");
                        } else if ("warning".equalsIgnoreCase(item.getSeverity())) {
                            icon.setIconLiteral("fas-exclamation-circle");
                            icon.setStyle("-fx-text-fill: #ffaa00;");
                        } else {
                            icon.setIconLiteral("fas-info-circle");
                            icon.setStyle("-fx-text-fill: #4444ff;");
                        }
                    } else {
                        icon.setIconLiteral("fas-lightbulb");
                        icon.setStyle("-fx-text-fill: #44aa44;");
                    }
                    
                    // Create HBox with icon and text
                    HBox content = new HBox(8, icon, new Label("[" + item.getLineNumber() + "] " + item.getMessage()));
                    content.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                    
                    setGraphic(content);
                    setText(null);
                    setTooltip(new Tooltip(item.getMessage()));
                }
            }
        };
    }

    private void onIssueClicked(MouseEvent event) {
        @SuppressWarnings("unchecked")
        ListView<CodeIssue> source = (ListView<CodeIssue>) event.getSource();
        CodeIssue selected = source.getSelectionModel().getSelectedItem();
        if (selected != null && selected.getLineNumber() > 0) {
            // Clear any previous selected highlights first
            clearAllLineHighlights();
            // Re-apply all issue highlights
            for (CodeIssue issue : errors) {
                String styleClass = getHighlightClass(issue);
                highlightLine(issue.getLineNumber(), styleClass);
            }
            for (CodeIssue issue : suggestions) {
                if (issue.getType() == CodeIssue.Type.SUGGESTION) {
                    highlightLine(issue.getLineNumber(), "suggestion-highlight");
                }
            }
            // Now highlight the selected line
            highlightLine(selected.getLineNumber(), "selected-issue-highlight");
            showIssueDetails(selected);
        }
    }

    private void showIssueDetails(CodeIssue issue) {
        StringBuilder sb = new StringBuilder();
        sb.append("Line: ").append(issue.getLineNumber()).append("\n");
        sb.append("Type: ").append(issue.getType()).append("\n");
        sb.append("Severity: ").append(issue.getSeverity()).append("\n");
        sb.append("Message: ").append(issue.getMessage()).append("\n");
        detailsArea.setText(sb.toString());
    }

    private void highlightLine(int lineNumber, String styleClass) {
        if (lineNumber < 1) return;
        int start = codeArea.position(lineNumber - 1, 0).toOffset();
        int end = codeArea.position(lineNumber - 1, codeArea.getParagraph(lineNumber - 1).length()).toOffset();
        codeArea.setStyleClass(start, end, styleClass);
    }

    private void clearAllLineHighlights() {
        codeArea.setStyleSpans(0, codeArea.getStyleSpans(0, codeArea.getLength()));
    }
    
    private void renderHeatmap(CodeAnalysisResult result) {
        // Clear existing heatmap
        heatmapPane.getChildren().clear();
        
        // Get total number of lines
        int totalLines = codeArea.getParagraphs().size();
        
        // Create a map to track issue density per line
        Map<Integer, String> lineSeverity = new HashMap<>();
        
        // Process all issues and determine severity for each line
        for (CodeIssue issue : result.getErrors()) {
            int lineNum = issue.getLineNumber();
            String currentSeverity = lineSeverity.get(lineNum);
            String issueSeverity = issue.getSeverity();
            
            // Prioritize severity: critical > warning > info
            if (currentSeverity == null || 
                (issueSeverity.equalsIgnoreCase("critical") && !currentSeverity.equalsIgnoreCase("critical")) ||
                (issueSeverity.equalsIgnoreCase("warning") && currentSeverity.equalsIgnoreCase("info"))) {
                lineSeverity.put(lineNum, issueSeverity);
            }
        }
        
        // Create rectangles for each line
        for (int lineNumber = 1; lineNumber <= totalLines; lineNumber++) {
            Rectangle rect = new Rectangle();
            rect.setHeight(3);
            rect.setWidth(25); // Match the heatmap pane width
            
            // Set color based on severity
            String severity = lineSeverity.get(lineNumber);
            if (severity != null) {
                if (severity.equalsIgnoreCase("critical")) {
                    rect.setFill(javafx.scene.paint.Color.web("#dc2626"));
                } else if (severity.equalsIgnoreCase("warning")) {
                    rect.setFill(javafx.scene.paint.Color.web("#fbbf24"));
                } else {
                    rect.setFill(javafx.scene.paint.Color.web("#60a5fa"));
                }
            } else {
                rect.setFill(javafx.scene.paint.Color.web("#34d399"));
            }
            
            // Add click handler to scroll to line
            final int lineNum = lineNumber;
            rect.setOnMouseClicked(e -> codeArea.showParagraphAtTop(lineNum - 1));
            
            heatmapPane.getChildren().add(rect);
        }
    }

    private void exportAnalysisAsMarkdown() {
        if (errors.isEmpty() && suggestions.isEmpty()) {
            // statusBar.setText("No analysis to export."); // removed
            return;
        }
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Analysis Report");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Markdown Files", "*.md"));
        File file = fileChooser.showSaveDialog(primaryStage);
        if (file != null) {
            try (FileWriter writer = new FileWriter(file)) {
                writer.write("# Code Analysis Report\n\n");
                writer.write("## Errors\n");
                for (CodeIssue issue : errors) {
                    writer.write("- Line " + issue.getLineNumber() + " [" + issue.getSeverity() + "]: " + issue.getMessage() + "\n");
                }
                writer.write("\n## Suggestions\n");
                for (CodeIssue issue : suggestions) {
                    writer.write("- Line " + issue.getLineNumber() + ": " + issue.getMessage() + "\n");
                }
                // statusBar.setText("Exported analysis to " + file.getName()); // removed
            } catch (IOException ex) {
                // statusBar.setText("Failed to export: " + ex.getMessage()); // removed
            }
        }
    }

    // AI-related methods have been removed

    public String getCurrentCode() {
        return codeArea.getText();
    }
    
    public void replaceCodeText(String newCode) {
        codeArea.replaceText(newCode);
    }
    
    /**
     * Toggle visibility of the AI suggestion panel
     */
    public void toggleAiSuggestionPanel() {
        boolean isVisible = aiSuggestionArea.isVisible();
        aiSuggestionArea.setVisible(!isVisible);
        if (isVisible) {
            // Hide panel and adjust divider
            codeSplitPane.setDividerPositions(1.0);
        } else {
            // Show panel and adjust divider
            codeSplitPane.setDividerPositions(0.7);
            if (aiSuggestionArea.getText().isEmpty()) {
                aiSuggestionArea.setText("Ask AI for suggestions about your code by clicking the 'Ask AI' button.");
            }
        }
    }
    
    /**
     * Show loading indicator while waiting for AI response
     */
    public void showAiLoading() {
        aiProgressIndicator.setVisible(true);
    }
    
    /**
     * Hide loading indicator
     */
    public void hideAiLoading() {
        aiProgressIndicator.setVisible(false);
    }
    
    /**
     * Show AI suggestion in the panel and ensure it's visible
     */
    public void showAiSuggestion(String suggestion) {
        aiSuggestionArea.setText(suggestion);
        if (!aiSuggestionArea.isVisible()) {
            aiSuggestionArea.setVisible(true);
            codeSplitPane.setDividerPositions(0.7);
        }
    }
    
    /**
     * Append to the AI suggestion text (for streaming responses)
     */
    public void appendAiSuggestion(String text) {
        aiSuggestionArea.appendText(text);
        if (!aiSuggestionArea.isVisible()) {
            aiSuggestionArea.setVisible(true);
            codeSplitPane.setDividerPositions(0.7);
        }
    }
    
    /**
     * Clear the AI suggestion panel
     */
    public void clearAiSuggestion() {
        aiSuggestionArea.clear();
    }
    
    /**
     * Update the UI when code is loaded
     */
    public void onCodeLoaded() {
        aiSuggestionArea.setVisible(true);
    }
    
    /**
     * Request a full AI review of the code
     */
    private void requestAiReview() {
        String code = getCurrentCode();
        if (code == null || code.trim().isEmpty()) {
            displayError("No code to analyze");
            return;
        }
        
        showAiLoading();
        clearAiSuggestion();
        
        // Show AI suggestion area with initial message
        aiSuggestionArea.setVisible(true);
        codeSplitPane.setDividerPositions(0.7);
        appendAiSuggestion("Initializing AI code review...\n\n");
        
        // Add a prompt specifically asking for a code review with a cleaner format
        String codeWithPrompt = "TASK: Perform a professional code review on this Java code.\n\n" + 
                                "CODE:\n```java\n" + code + "\n```\n\n" +
                                "REVIEW GUIDELINES:\n" +
                                "1. Identify any critical bugs or errors\n" +
                                "2. Suggest improvements for code quality and readability\n" +
                                "3. Comment on performance issues\n" +
                                "4. Recommend best practices\n\n" +
                                "FORMAT: Provide a structured review with specific line numbers where applicable. Start with a brief summary.";
        
        // Use streaming API for better user experience
        CompletableFuture<Void> future = controller.getAiService().getCodeSuggestionStreaming(
                codeWithPrompt, 
                // This consumer will be called for each chunk of the response
                chunk -> Platform.runLater(() -> appendAiSuggestion(chunk))
        );
        
        // Handle completion
        future.exceptionally(ex -> {
            Platform.runLater(() -> {
                appendAiSuggestion("\n\n❌ Error getting AI review: " + ex.getMessage());
                appendAiSuggestion("\n\nTroubleshooting steps:\n" +
                                  "1. Check your internet connection\n" +
                                  "2. Verify your API key in application-private.properties\n" +
                                  "3. Try again with a smaller code sample");
                hideAiLoading();
            });
            return null;
        }).thenRun(() -> {
            Platform.runLater(() -> {
                // If the response is empty or very short, provide guidance
                if (aiSuggestionArea.getText().trim().length() < 100) {
                    appendAiSuggestion("\n\nThe AI response appears to be incomplete or empty. You can try:\n" +
                                      "- Refreshing the review request\n" +
                                      "- Trying a simpler or shorter code sample\n" +
                                      "- Checking your network connection");
                }
                hideAiLoading();
            });
        });
    }
} 