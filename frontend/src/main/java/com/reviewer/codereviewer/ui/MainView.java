package com.reviewer.codereviewer.ui;

import com.reviewer.codereviewer.controller.MainController;
import com.reviewer.codereviewer.client.dto.CodeAnalysisResult;
import com.reviewer.codereviewer.client.dto.CodeIssue;
import com.reviewer.codereviewer.client.dto.QuickFix;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.scene.control.SplitPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import javafx.geometry.Pos;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import java.io.FileWriter;
import java.io.IOException;
import java.io.File;
import java.util.logging.Logger;

import javafx.scene.control.Tooltip;
import javafx.util.Duration;
import java.util.HashMap;
import java.util.Map;
import javafx.scene.layout.StackPane;
import javafx.geometry.Insets;
import javafx.scene.text.Text;
import javafx.scene.shape.Rectangle;

public class MainView {
    private final MainController controller;
    private final Logger logger = Logger.getLogger(MainView.class.getName());
    private CodeArea codeArea;
    private TableView<CodeIssue> errorTableView;
    private TableView<CodeIssue> suggestionTableView;
    private ObservableList<CodeIssue> errors;
    private ObservableList<CodeIssue> suggestions;
    private FilteredList<CodeIssue> filteredErrors;
    private FilteredList<CodeIssue> filteredSuggestions;
    private Stage primaryStage;
    private Label detailsHeader;
    private TextArea detailsArea;
    private Map<Integer, String> errorTooltips = new HashMap<>();
    private VBox heatmapPane;
    
    // Search and filter components
    private TextField searchField;
    private ToggleButton criticalToggle;
    private ToggleButton warningToggle;
    private ToggleButton infoToggle;
    
    // Status bar
    private Label statusBar;
    
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
        root.setPrefSize(1400, 900);
        
        // Initialize observable lists
        errors = javafx.collections.FXCollections.observableArrayList();
        suggestions = javafx.collections.FXCollections.observableArrayList();
        filteredErrors = new FilteredList<>(errors, p -> true);
        filteredSuggestions = new FilteredList<>(suggestions, p -> true);
        
        // Initialize error table view
        errorTableView = new TableView<>(filteredErrors);
        TableColumn<CodeIssue, Integer> errLineCol = new TableColumn<>("Line");
        TableColumn<CodeIssue, String> errMessageCol = new TableColumn<>("Message");
        errLineCol.setCellValueFactory(new PropertyValueFactory<>("lineNumber"));
        errMessageCol.setCellValueFactory(new PropertyValueFactory<>("message"));
        errorTableView.getColumns().add(errLineCol);
        errorTableView.getColumns().add(errMessageCol);
        errorTableView.setPrefHeight(250);
        errorTableView.setMinHeight(200);
        errorTableView.getStyleClass().add("table-view");
        
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
        HBox codeWithHeatmap = new HBox(5);
        codeWithHeatmap.getChildren().addAll(codeArea, heatmapPane);
        HBox.setHgrow(codeArea, javafx.scene.layout.Priority.ALWAYS);
        
        // Create AI suggestion area
        aiSuggestionArea = new TextArea();
        aiSuggestionArea.setEditable(false);
        aiSuggestionArea.setWrapText(true);
        aiSuggestionArea.setPrefRowCount(10);
        aiSuggestionArea.setPromptText("AI code review suggestions will appear here...");
        aiSuggestionArea.getStyleClass().add("ai-suggestion-area");
        aiSuggestionArea.setVisible(false);  // Hidden by default
        
        // Create a split pane for code area and AI suggestions
        codeSplitPane = new SplitPane();
        codeSplitPane.setOrientation(javafx.geometry.Orientation.VERTICAL);
        codeSplitPane.getItems().addAll(codeWithHeatmap, aiSuggestionArea);
        codeSplitPane.setDividerPositions(1.0);  // AI area hidden initially
        SplitPane.setResizableWithParent(aiSuggestionArea, false);
        VBox.setVgrow(codeSplitPane, javafx.scene.layout.Priority.ALWAYS);
        
        // Create button for AI review
        Button reviewButton = new Button("🤖 AI Code Review");
        reviewButton.getStyleClass().add("ai-button");
        reviewButton.setOnAction(e -> requestAiReview());

        aiProgressIndicator = new ProgressIndicator();
        aiProgressIndicator.setMaxSize(20, 20);
        aiProgressIndicator.setVisible(false);

        HBox aiButtonBox = new HBox(5, reviewButton, aiProgressIndicator);
        aiButtonBox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        aiButtonBox.setPadding(new Insets(5));

        // Add the AI button to the code area header
        HBox codeHeader = new HBox();
        codeHeader.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        codeHeader.getChildren().add(aiButtonBox);
        codeHeader.setPadding(new Insets(5, 10, 5, 10));
        codeHeader.setSpacing(10);
        // Create and configure suggestion TableView
        suggestionTableView = new TableView<>(filteredSuggestions);
        TableColumn<CodeIssue, Integer> suggLineCol = new TableColumn<>("Line");
        TableColumn<CodeIssue, String> suggMessageCol = new TableColumn<>("Message");
        
        suggLineCol.setCellValueFactory(new PropertyValueFactory<>("lineNumber"));
        suggMessageCol.setCellValueFactory(new PropertyValueFactory<>("message"));
        
        suggestionTableView.getColumns().add(suggLineCol);
        suggestionTableView.getColumns().add(suggMessageCol);
        suggestionTableView.setPrefHeight(250);
        suggestionTableView.setMinHeight(200);
        suggestionTableView.getStyleClass().add("table-view");
        
        // Add selection listeners
        errorTableView.setOnMouseClicked(this::onIssueClicked);
        suggestionTableView.setOnMouseClicked(this::onIssueClicked);
        
        // Create filter controls
        searchField = new TextField();
        searchField.setPromptText("Search issues...");
        searchField.getStyleClass().add("search-field");
        
        criticalToggle = new ToggleButton("Critical");
        warningToggle = new ToggleButton("Warning");
        infoToggle = new ToggleButton("Info");
        
        HBox filterBox = new HBox(8);
        filterBox.getChildren().addAll(
            searchField,
            new Separator(javafx.geometry.Orientation.VERTICAL),
            criticalToggle, warningToggle, infoToggle
        );
        filterBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        filterBox.getStyleClass().add("filter-box");
        
        // Setup filter listeners
        setupFilterListeners();

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
        VBox analysisPane = new VBox();
        analysisPane.setSpacing(15);
        analysisPane.setPadding(new Insets(15));
        analysisPane.getChildren().addAll(
            errorHeader, filterBox, errorTableView, 
            suggestionHeader, suggestionTableView
        );
        analysisPane.getStyleClass().add("analysis-pane");
        analysisPane.setMaxWidth(400);
        analysisPane.setPrefWidth(350);
        analysisPane.setMinWidth(300);

        VBox detailsPane = new VBox();
        detailsPane.setSpacing(10);
        detailsPane.setPadding(new Insets(15));
        detailsPane.getChildren().addAll(detailsHeader, detailsArea);
        detailsPane.getStyleClass().add("details-pane");
        detailsPane.setMaxWidth(400);
        detailsPane.setPrefWidth(350);
        detailsPane.setMinWidth(300);

        // Main split pane: left = analysis, right = details
        SplitPane mainSplitPane = new SplitPane();
        mainSplitPane.getItems().addAll(analysisPane, detailsPane);
        mainSplitPane.setDividerPositions(0.6);
        mainSplitPane.getStyleClass().add("main-split-pane");

        // Set proper growth behavior for both sides
        SplitPane.setResizableWithParent(analysisPane, true);
        SplitPane.setResizableWithParent(detailsPane, true);

        MenuBar menuBar = new MenuBar();
        Menu fileMenu = new Menu("File");
        // Create menu items with icons
        MenuItem openItem = new MenuItem("Open Java File...");
        FontIcon openIcon = new FontIcon(FontAwesomeSolid.FOLDER_OPEN);
        openIcon.setIconSize(16);
        openItem.setGraphic(openIcon);
        openItem.setAccelerator(new KeyCodeCombination(KeyCode.O, KeyCombination.SHORTCUT_DOWN));
        openItem.setOnAction(e -> controller.onOpenFile(primaryStage));
        
        MenuItem exportItem = new MenuItem("Export Analysis as Markdown");
        FontIcon exportIcon = new FontIcon(FontAwesomeSolid.DOWNLOAD);
        exportIcon.setIconSize(16);
        exportItem.setGraphic(exportIcon);
        exportItem.setAccelerator(new KeyCodeCombination(KeyCode.E, KeyCombination.SHORTCUT_DOWN));
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
        
        // Create Edit menu for undo/redo
        Menu editMenu = new Menu("Edit");
        MenuItem undoItem = new MenuItem("Undo");
        undoItem.setAccelerator(new KeyCodeCombination(KeyCode.Z, KeyCombination.SHORTCUT_DOWN));
        undoItem.setOnAction(e -> {
            statusBar.setText("Undo/Redo functionality has been moved to the backend server");
        });
        FontIcon undoIcon = new FontIcon(FontAwesomeSolid.UNDO);
        undoIcon.setIconSize(16);
        undoItem.setGraphic(undoIcon);
        
        MenuItem redoItem = new MenuItem("Redo");
        redoItem.setAccelerator(new KeyCodeCombination(KeyCode.Z, KeyCombination.SHORTCUT_DOWN, KeyCombination.SHIFT_DOWN));
        redoItem.setOnAction(e -> {
            statusBar.setText("Undo/Redo functionality has been moved to the backend server");
        });
        FontIcon redoIcon = new FontIcon(FontAwesomeSolid.REDO);
        redoIcon.setIconSize(16);
        redoItem.setGraphic(redoIcon);
        
        editMenu.getItems().addAll(undoItem, redoItem);
        
        menuBar.getMenus().addAll(fileMenu, editMenu, aiMenu);
        menuBar.getStyleClass().add("menu-bar");

        // Create and setup status bar
        statusBar = new Label("Ready");
        statusBar.getStyleClass().add("status-bar");

        // Create outer split pane: left = code with AI suggestions, right = analysis + details
        SplitPane outerSplitPane = new SplitPane();
        outerSplitPane.setOrientation(javafx.geometry.Orientation.HORIZONTAL);
        
        // Left side container with code and header
        VBox leftContainer = new VBox();
        leftContainer.getChildren().addAll(codeHeader, codeSplitPane);
        VBox.setVgrow(codeSplitPane, javafx.scene.layout.Priority.ALWAYS);
        
        // Right side with analysis and details
        outerSplitPane.getItems().addAll(leftContainer, mainSplitPane);
        outerSplitPane.setDividerPositions(0.5);
        
        root.setTop(menuBar);
        root.setCenter(outerSplitPane);
        root.setBottom(statusBar);

        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/app-theme.css").toExternalForm());
        scene.getStylesheets().add(getClass().getResource("/ai-style.css").toExternalForm());

        // Add keyboard shortcuts for undo/redo
        scene.getAccelerators().put(
            new KeyCodeCombination(KeyCode.Z, KeyCombination.SHORTCUT_DOWN),
            () -> statusBar.setText("Undo/Redo functionality has been moved to the backend server")
        );
        
        scene.getAccelerators().put(
            new KeyCodeCombination(KeyCode.Z, KeyCombination.SHORTCUT_DOWN, KeyCombination.SHIFT_DOWN),
            () -> statusBar.setText("Undo/Redo functionality has been moved to the backend server")
        );
        
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
            highlightLine(issue.getLine(), styleClass);
            errorTooltips.put(issue.getLine(), issue.getMessage());
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
                highlightLine(issue.getLine(), "suggestion-highlight");
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



    private void setupFilterListeners() {
        // Setup search field listener
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            String searchText = newVal.toLowerCase();
            filteredErrors.setPredicate(issue -> 
                issue.getMessage().toLowerCase().contains(searchText) &&
                matchesSeverityFilter(issue)
            );
            filteredSuggestions.setPredicate(issue ->
                issue.getMessage().toLowerCase().contains(searchText)
            );
        });

        // Setup severity toggle listeners
        ToggleButton[] toggles = {criticalToggle, warningToggle, infoToggle};
        for (ToggleButton toggle : toggles) {
            toggle.setSelected(true);
            toggle.selectedProperty().addListener((obs, oldVal, newVal) -> {
                filteredErrors.setPredicate(issue ->
                    issue.getMessage().toLowerCase().contains(searchField.getText().toLowerCase()) &&
                    matchesSeverityFilter(issue)
                );
            });
        }
    }

    private boolean matchesSeverityFilter(CodeIssue issue) {
        String severity = issue.getSeverity().toLowerCase();
        return (severity.equals("critical") && criticalToggle.isSelected()) ||
               (severity.equals("warning") && warningToggle.isSelected()) ||
               (severity.equals("info") && infoToggle.isSelected());
    }

    private void onIssueClicked(MouseEvent event) {
        Object source = event.getSource();
        if (!(source instanceof TableView<?>)) {
            return;
        }
        @SuppressWarnings("unchecked")
        TableView<CodeIssue> tableView = (TableView<CodeIssue>) source;
        CodeIssue selected = tableView.getSelectionModel().getSelectedItem();
        if (selected != null && selected.getLine() > 0) {
            // Clear any previous selected highlights first
            clearAllLineHighlights();
            // Re-apply all issue highlights
            for (CodeIssue issue : errors) {
                String styleClass = getHighlightClass(issue);
                highlightLine(issue.getLine(), styleClass);
            }
            for (CodeIssue issue : suggestions) {
                if (issue.getType() == CodeIssue.Type.SUGGESTION) {
                    highlightLine(issue.getLine(), "suggestion-highlight");
                }
            }
            // Now highlight the selected line
            highlightLine(selected.getLine(), "selected-issue-highlight");
            showIssueDetails(selected);
            
            // Scroll to the selected line and center it in the viewport
            Platform.runLater(() -> {
                codeArea.showParagraphAtCenter(selected.getLine() - 1);
                // Also request focus on the code area for better UX
                codeArea.requestFocus();
            });
        }
    }

    private void showIssueDetails(CodeIssue issue) {
        // Create a VBox to hold both the details and quick fix buttons
        VBox detailsContainer = new VBox(10); // 10px spacing
        
        // Add the text details
        StringBuilder sb = new StringBuilder();
        sb.append("Line: ").append(issue.getLine()).append("\n");
        sb.append("Type: ").append(issue.getType()).append("\n");
        sb.append("Severity: ").append(issue.getSeverity()).append("\n");
        sb.append("Message: ").append(issue.getMessage()).append("\n");
        
        TextArea textDetails = new TextArea(sb.toString());
        textDetails.setEditable(false);
        textDetails.setWrapText(true);
        textDetails.setPrefRowCount(4);
        
        detailsContainer.getChildren().add(textDetails);
        
        // If there are quick fixes available, add buttons for them
        if (issue.hasQuickFixes()) {
            HBox quickFixButtons = new HBox(5); // 5px spacing
            quickFixButtons.setAlignment(Pos.CENTER_LEFT);
            
            for (QuickFix fix : issue.getQuickFixes()) {
                Button fixButton = new Button(fix.getDescription());
                fixButton.getStyleClass().add("quick-fix-button");
                
                // Add tooltip with detailed description
                Tooltip tooltip = new Tooltip(fix.getDescription());
                tooltip.setShowDelay(Duration.millis(200));
                fixButton.setTooltip(tooltip);
                
                // Add click handler
                fixButton.setOnAction(e -> applyQuickFix(issue, fix));
                
                quickFixButtons.getChildren().add(fixButton);
            }
            
            detailsContainer.getChildren().add(quickFixButtons);
        }
        
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
            int lineNum = issue.getLine();
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
            statusBar.setText("No analysis to export");
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
                    writer.write("- Line " + issue.getLine() + " [" + issue.getSeverity() + "]: " + issue.getMessage() + "\n");
                }
                writer.write("\n## Suggestions\n");
                for (CodeIssue issue : suggestions) {
                    writer.write("- Line " + issue.getLine() + ": " + issue.getMessage() + "\n");
                }
                statusBar.setText("Analysis exported to " + file.getName());
            } catch (IOException ex) {
                statusBar.setText("Failed to export: " + ex.getMessage());
            }
        }
    }

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
        
        FadeTransition fade = new FadeTransition(Duration.millis(300), aiSuggestionArea);
        fade.setFromValue(isVisible ? 1.0 : 0.0);
        fade.setToValue(isVisible ? 0.0 : 1.0);
        
        if (isVisible) {
            fade.setOnFinished(e -> {
                aiSuggestionArea.setVisible(false);
                codeSplitPane.setDividerPositions(1.0);
            });
        } else {
            aiSuggestionArea.setVisible(true);
            aiSuggestionArea.setOpacity(0);
            codeSplitPane.setDividerPositions(0.7);
            if (aiSuggestionArea.getText().isEmpty()) {
                aiSuggestionArea.setText("Ask AI for suggestions about your code by clicking the 'Ask AI' button.");
            }
        }
        
        fade.play();
        
        // Update status bar
        statusBar.setText(isVisible ? "AI suggestion panel hidden" : "AI suggestion panel shown");
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
        logger.info("appendAiSuggestion called with text length: " + text.length() + " chars");
        logger.info("aiSuggestionArea is null: " + (aiSuggestionArea == null));
        logger.info("aiSuggestionArea is visible: " + (aiSuggestionArea != null && aiSuggestionArea.isVisible()));
        aiSuggestionArea.appendText(text);
        if (!aiSuggestionArea.isVisible()) {
            logger.info("Making aiSuggestionArea visible");
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
     * Applies a quick fix to the code and updates the UI.
     * @param issue The issue to fix
     * @param fix The quick fix to apply
     */
    private void applyQuickFix(CodeIssue issue, QuickFix fix) {
        statusBar.setText("Quick fix functionality has been moved to the backend server");
        
        // In a real implementation, this would send a request to the backend
        // to apply the quick fix and then update the UI with the result
        
        logger.info("Quick fix requested: " + fix.getDescription() + " for issue: " + issue.getMessage());
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
        
        // Use the controller's method which now talks to the backend
        String reviewPrompt = "Please provide a comprehensive code review focusing on:\n" +
                             "1. Code quality issues\n" +
                             "2. Performance optimizations\n" +
                             "3. Security concerns\n" +
                             "4. Best practices recommendations\n\n" +
                             "FORMAT: Provide a structured review with specific line numbers where applicable. Start with a brief summary.";
        
        // Use the controller's AI method which now uses the API client
        controller.onAskAiWithQuestion(code, reviewPrompt);
    }
} 