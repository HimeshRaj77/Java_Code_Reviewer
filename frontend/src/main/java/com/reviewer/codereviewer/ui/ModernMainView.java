package com.reviewer.codereviewer.ui;

import com.reviewer.codereviewer.controller.MainController;
import com.reviewer.codereviewer.client.dto.CodeAnalysisResult;
import com.reviewer.codereviewer.client.dto.CodeIssue;
import com.reviewer.codereviewer.client.dto.QuickFix;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.*;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;
import java.io.FileWriter;
import java.io.IOException;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Modern, professional UI for Code Reviewer with improved information hierarchy
 * and clean visual design following modern UI/UX principles.
 */
public class ModernMainView {
    private static final Logger logger = Logger.getLogger(ModernMainView.class.getName());
    
    private final MainController controller;
    
        // UI Components
    private CodeArea codeArea;
    private TabPane analysisTabPane;
    private TableView<CodeIssue> allIssuesTable;
    private TableView<CodeIssue> criticalIssuesTable;
    private TableView<CodeIssue> warningsTable;
    private TableView<CodeIssue> suggestionsTable;
    private Label statusLabel;
    private ProgressBar analysisProgressBar;
    private Button analyzeButton;
    private Button aiReviewButton;
    private VBox issueDetailsPane;
    private ScrollPane detailsScrollPane;
    private Label filePathLabel;
    private VBox heatmapPane;
    private TextField searchField;
    private ProgressIndicator aiProgressIndicator;
    private TextArea aiSuggestionArea;
    private SplitPane mainSplit;
    private SplitPane outerSplit;
    private BorderPane content;
    
    // Data and State
    private ObservableList<CodeIssue> allIssues = FXCollections.observableArrayList();
    private ObservableList<CodeIssue> criticalIssues = FXCollections.observableArrayList();
    private ObservableList<CodeIssue> warnings = FXCollections.observableArrayList();
    private ObservableList<CodeIssue> suggestions = FXCollections.observableArrayList();
    private FilteredList<CodeIssue> filteredIssues;
    private File currentFile;
    private Map<Integer, Rectangle> heatmapRectangles = new HashMap<>();
    public ModernMainView(MainController controller) {
        this.controller = controller;
        this.filteredIssues = new FilteredList<>(allIssues);
        setupKeyboardShortcuts();
    }
    
    public Scene createScene() {
        BorderPane root = createMainLayout();
        Scene scene = new Scene(root, 1400, 900);
        
        // Apply modern theme
        scene.getStylesheets().clear();
        scene.getStylesheets().add(getClass().getResource("/modern-theme.css").toExternalForm());
        
        return scene;
    }
    
    private BorderPane createMainLayout() {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("main-container");
        
        // Create header with file info and controls
        VBox header = createHeader();
        root.setTop(header);
        
        // Create main content area with improved layout
        BorderPane content = createContentArea();
        root.setCenter(content);
        
        // Create status bar
        HBox statusBar = createStatusBar();
        root.setBottom(statusBar);
        
        return root;
    }
    
    private VBox createHeader() {
        VBox header = new VBox();
        header.getStyleClass().add("header-bar");
        
        // Create menu bar
        MenuBar menuBar = createMenuBar();
        
        // Create toolbar with file operations and analysis controls
        HBox toolbar = createToolbar();
        
        header.getChildren().addAll(menuBar, toolbar);
        return header;
    }
    
    private MenuBar createMenuBar() {
        MenuBar menuBar = new MenuBar();
        
        // File Menu
        Menu fileMenu = new Menu("File");
        MenuItem openItem = new MenuItem("Open File...");
        openItem.setAccelerator(new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN));
        openItem.setOnAction(e -> openFile());
        
        MenuItem saveItem = new MenuItem("Save");
        saveItem.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN));
        saveItem.setOnAction(e -> saveFile());
        
        MenuItem exitItem = new MenuItem("Exit");
        exitItem.setOnAction(e -> Platform.exit());
        
        fileMenu.getItems().addAll(openItem, saveItem, new SeparatorMenuItem(), exitItem);
        
        // Analysis Menu
        Menu analysisMenu = new Menu("Analysis");
        MenuItem analyzeItem = new MenuItem("Analyze Code");
        analyzeItem.setAccelerator(new KeyCodeCombination(KeyCode.F5));
        analyzeItem.setOnAction(e -> analyzeCode());
        
        MenuItem aiReviewItem = new MenuItem("AI Code Review");
        aiReviewItem.setAccelerator(new KeyCodeCombination(KeyCode.F6));
        aiReviewItem.setOnAction(e -> requestAiReview());
        
        analysisMenu.getItems().addAll(analyzeItem, aiReviewItem);
        
        // View Menu
        Menu viewMenu = new Menu("View");
        CheckMenuItem showHeatmapItem = new CheckMenuItem("Show Heatmap");
        showHeatmapItem.setSelected(true);
        showHeatmapItem.setOnAction(e -> toggleHeatmap(showHeatmapItem.isSelected()));
        
        viewMenu.getItems().add(showHeatmapItem);
        
        menuBar.getMenus().addAll(fileMenu, analysisMenu, viewMenu);
        return menuBar;
    }
    
    private HBox createToolbar() {
        HBox toolbar = new HBox(16);
        toolbar.getStyleClass().add("toolbar");
        toolbar.setAlignment(Pos.CENTER_LEFT);
        
        // File path display
        filePathLabel = new Label("No file selected");
        filePathLabel.getStyleClass().add("file-path-label");
        
        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Analysis controls
        analyzeButton = new Button("📊 Analyze Code");
        analyzeButton.getStyleClass().addAll("button", "primary-button");
        analyzeButton.setOnAction(e -> analyzeCode());
        
        aiReviewButton = new Button("🤖 AI Review");
        aiReviewButton.getStyleClass().addAll("button", "ai-button");
        aiReviewButton.setOnAction(e -> requestAiReview());
        
        // Progress indicator
        analysisProgressBar = new ProgressBar();
        analysisProgressBar.setVisible(false);
        analysisProgressBar.setPrefWidth(200);
        
        toolbar.getChildren().addAll(
            filePathLabel, spacer, 
            analyzeButton, aiReviewButton, analysisProgressBar
        );
        
        return toolbar;
    }
    
    private BorderPane createContentArea() {
        content = new BorderPane();
        content.getStyleClass().add("content-area");
        
        // Left: Code editor with heatmap
        VBox codeSection = createCodeSection();
        
        // Center: Analysis results
        VBox analysisSection = createAnalysisSection();
        
        // Split pane for resizable layout (initially just code and analysis)
        mainSplit = new SplitPane();
        mainSplit.getItems().addAll(codeSection, analysisSection);
        mainSplit.setDividerPositions(0.65);
        
        // Initially show only the main split without details panel
        content.setCenter(mainSplit);
        return content;
    }
    
    private VBox createCodeSection() {
        VBox codeSection = new VBox(12);
        codeSection.getStyleClass().add("card");
        
        // Code editor header
        HBox codeHeader = new HBox(12);
        codeHeader.setAlignment(Pos.CENTER_LEFT);
        
        Label codeTitle = new Label("📝 Code Editor");
        codeTitle.getStyleClass().addAll("card-header", "section-header");
        
        // Search in code
        TextField codeSearchField = new TextField();
        codeSearchField.setPromptText("Search in code...");
        codeSearchField.getStyleClass().add("search-field");
        codeSearchField.setPrefWidth(200);
        
        Region codeSpacerJavaFX = new Region();
        HBox.setHgrow(codeSpacerJavaFX, Priority.ALWAYS);
        
        codeHeader.getChildren().addAll(codeTitle, codeSpacerJavaFX, codeSearchField);
        
        // Code area with heatmap
        HBox codeContainer = createCodeAreaWithHeatmap();
        VBox.setVgrow(codeContainer, Priority.ALWAYS);
        
        codeSection.getChildren().addAll(codeHeader, codeContainer);
        return codeSection;
    }
    
    private HBox createCodeAreaWithHeatmap() {
        HBox container = new HBox(0);
        
        // Initialize code area
        codeArea = new CodeArea();
        codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));
        codeArea.getStyleClass().add("code-area");
        codeArea.setWrapText(false);
        
        // Enable syntax highlighting (basic)
        codeArea.textProperty().addListener((obs, oldText, newText) -> {
            // Basic Java syntax highlighting could be added here
        });
        
        // Heatmap panel
        heatmapPane = new VBox();
        heatmapPane.getStyleClass().add("heatmap-pane");
        heatmapPane.setPrefWidth(20);
        heatmapPane.setMinWidth(20);
        heatmapPane.setMaxWidth(20);
        
        // Set growth priorities
        HBox.setHgrow(codeArea, Priority.ALWAYS);
        
        container.getChildren().addAll(codeArea, heatmapPane);
        return container;
    }
    
    private VBox createAnalysisSection() {
        VBox analysisSection = new VBox(16);
        analysisSection.getStyleClass().add("card");
        analysisSection.setPrefWidth(450);
        analysisSection.setMinWidth(400);
        
        // Analysis header with search
        HBox analysisHeader = createAnalysisHeader();
        
        // Tab pane for different issue types
        analysisTabPane = createAnalysisTabPane();
        VBox.setVgrow(analysisTabPane, Priority.ALWAYS);
        
        analysisSection.getChildren().addAll(analysisHeader, analysisTabPane);
        return analysisSection;
    }
    
    private HBox createAnalysisHeader() {
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        
        Label title = new Label("🔍 Analysis Results");
        title.getStyleClass().addAll("card-header", "section-header");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Global search for issues
        searchField = new TextField();
        searchField.setPromptText("Search issues...");
        searchField.getStyleClass().add("search-field");
        searchField.setPrefWidth(180);
        
        // Setup search functionality
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            updateIssueFilter();
        });
        
        header.getChildren().addAll(title, spacer, searchField);
        return header;
    }
    
    private TabPane createAnalysisTabPane() {
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        
        // Critical Issues Tab
        Tab criticalTab = new Tab();
        criticalTab.setText("🚨 Critical");
        criticalTab.setContent(createIssueTable(criticalIssuesTable = new TableView<>(), "critical"));
        
        // Warnings Tab
        Tab warningsTab = new Tab();
        warningsTab.setText("⚠️ Warnings");
        warningsTab.setContent(createIssueTable(warningsTable = new TableView<>(), "warning"));
        
        // Suggestions Tab
        Tab suggestionsTab = new Tab();
        suggestionsTab.setText("💡 Suggestions");
        suggestionsTab.setContent(createIssueTable(suggestionsTable = new TableView<>(), "suggestion"));
        
        // AI Review Tab
        Tab aiTab = new Tab();
        aiTab.setText("🤖 AI Review");
        aiTab.setContent(createAiReviewSection());
        
        tabPane.getTabs().addAll(criticalTab, warningsTab, suggestionsTab, aiTab);
        return tabPane;
    }
    
    private ScrollPane createIssueTable(TableView<CodeIssue> table, String type) {
        // Configure table
        table.getStyleClass().add("table-view");
        
        // Line column
        TableColumn<CodeIssue, Integer> lineCol = new TableColumn<>("Line");
        lineCol.setCellValueFactory(new PropertyValueFactory<>("lineNumber"));
        lineCol.setPrefWidth(50);
        lineCol.setMaxWidth(50);
        
        // Severity column with custom cell factory
        TableColumn<CodeIssue, String> severityCol = new TableColumn<>("Severity");
        severityCol.setCellValueFactory(new PropertyValueFactory<>("severity"));
        severityCol.setPrefWidth(80);
        severityCol.setCellFactory(column -> new TableCell<CodeIssue, String>() {
            @Override
            protected void updateItem(String severity, boolean empty) {
                super.updateItem(severity, empty);
                if (empty || severity == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label severityLabel = new Label(severity.toUpperCase());
                    severityLabel.getStyleClass().add("severity-" + severity.toLowerCase());
                    setGraphic(severityLabel);
                    setText(null);
                }
            }
        });
        
        // Message column
        TableColumn<CodeIssue, String> messageCol = new TableColumn<>("Description");
        messageCol.setCellValueFactory(new PropertyValueFactory<>("message"));
        messageCol.setPrefWidth(280);
        messageCol.setCellFactory(column -> new TableCell<CodeIssue, String>() {
            @Override
            protected void updateItem(String message, boolean empty) {
                super.updateItem(message, empty);
                if (empty || message == null) {
                    setText(null);
                } else {
                    setText(message);
                    setWrapText(true);
                }
            }
        });
        
        table.getColumns().addAll(lineCol, severityCol, messageCol);
        
        // Handle row selection
        table.setOnMouseClicked(this::onIssueClicked);
        
        // Set data based on type
        switch (type) {
            case "critical":
                table.setItems(criticalIssues);
                break;
            case "warning":
                table.setItems(warnings);
                break;
            case "suggestion":
                table.setItems(suggestions);
                break;
        }
        
        ScrollPane scrollPane = new ScrollPane(table);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        return scrollPane;
    }
    
    private VBox createAiReviewSection() {
        VBox aiSection = new VBox(12);
        aiSection.setPadding(new Insets(16));
        
        // AI progress and controls
        HBox aiControls = new HBox(12);
        aiControls.setAlignment(Pos.CENTER_LEFT);
        
        Button generateReviewBtn = new Button("Generate AI Review");
        generateReviewBtn.getStyleClass().addAll("button", "ai-button");
        generateReviewBtn.setOnAction(e -> requestAiReview());
        
        aiProgressIndicator = new ProgressIndicator();
        aiProgressIndicator.setMaxSize(20, 20);
        aiProgressIndicator.setVisible(false);
        
        Label aiStatus = new Label("Ready for AI review");
        aiStatus.getStyleClass().add("ai-status-label");
        
        aiControls.getChildren().addAll(generateReviewBtn, aiProgressIndicator, aiStatus);
        
        // AI suggestion area
        aiSuggestionArea = new TextArea();
        aiSuggestionArea.setPromptText("AI suggestions will appear here...");
        aiSuggestionArea.setEditable(false);
        aiSuggestionArea.setWrapText(true);
        aiSuggestionArea.getStyleClass().add("ai-suggestion-area");
        VBox.setVgrow(aiSuggestionArea, Priority.ALWAYS);
        
        aiSection.getChildren().addAll(aiControls, aiSuggestionArea);
        return aiSection;
    }
    
    private VBox createIssueDetailsPanel() {
        issueDetailsPane = new VBox(12);
        issueDetailsPane.getStyleClass().add("issue-details-panel");
        issueDetailsPane.setPadding(new Insets(16));
        issueDetailsPane.setVisible(false); // Hidden by default
        
        Label detailsTitle = new Label("� Issue Details");
        detailsTitle.getStyleClass().addAll("section-header", "details-header");
        
        // Create expandable details content
        VBox detailsContent = new VBox(8);
        detailsContent.getStyleClass().add("details-content");
        
        // Wrap in scroll pane for large content
        detailsScrollPane = new ScrollPane(detailsContent);
        detailsScrollPane.setFitToWidth(true);
        detailsScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        detailsScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        detailsScrollPane.getStyleClass().add("details-scroll");
        VBox.setVgrow(detailsScrollPane, Priority.ALWAYS);
        
        // Close button
        Button closeButton = new Button("✕");
        closeButton.getStyleClass().addAll("button", "close-button");
        closeButton.setOnAction(e -> hideIssueDetails());
        
        HBox titleBar = new HBox(8);
        titleBar.setAlignment(Pos.CENTER_LEFT);
        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        titleBar.getChildren().addAll(detailsTitle, spacer, closeButton);
        
        issueDetailsPane.getChildren().addAll(titleBar, detailsScrollPane);
        return issueDetailsPane;
    }
    
    private void showIssueDetails(CodeIssue issue) {
        if (issue == null) return;
        
        // Create the details panel if it doesn't exist
        if (issueDetailsPane == null) {
            createIssueDetailsPanel();
        }
        
        // Create detailed content for the selected issue
        VBox content = new VBox(12);
        content.getStyleClass().add("issue-details-content");
        
        // Issue header
        VBox header = new VBox(4);
        Label issueTitle = new Label("📌 " + issue.getType().toString() + " Issue");
        issueTitle.getStyleClass().add("issue-detail-title");
        
        Label issueSeverity = new Label("Severity: " + (issue.getSeverity() != null ? issue.getSeverity() : "Unknown"));
        issueSeverity.getStyleClass().add("issue-severity");
        
        Label issueLine = new Label("Line: " + issue.getLine());
        issueLine.getStyleClass().add("issue-line");
        
        header.getChildren().addAll(issueTitle, issueSeverity, issueLine);
        
        // Issue description
        Label descTitle = new Label("Description:");
        descTitle.getStyleClass().add("detail-section-title");
        
        TextArea descArea = new TextArea(issue.getMessage());
        descArea.setEditable(false);
        descArea.setWrapText(true);
        descArea.setPrefRowCount(3);
        descArea.getStyleClass().add("issue-description");
        
        // Quick fixes section
        VBox fixesSection = new VBox(8);
        Label fixesTitle = new Label("Suggested Fixes:");
        fixesTitle.getStyleClass().add("detail-section-title");
        
        if (issue.hasQuickFixes()) {
            for (QuickFix fix : issue.getQuickFixes()) {
                Label fixLabel = new Label("• " + fix.getDescription());
                fixLabel.getStyleClass().add("quick-fix-item");
                fixLabel.setWrapText(true);
                fixesSection.getChildren().add(fixLabel);
            }
        } else {
            Label noFixes = new Label("No quick fixes available");
            noFixes.getStyleClass().add("no-fixes-label");
            fixesSection.getChildren().add(noFixes);
        }
        
        // Note: Code context has been moved to backend processing
        
        content.getChildren().addAll(header, new VBox(4, descTitle, descArea), fixesSection);
        
        // Update the details scroll pane content
        detailsScrollPane.setContent(content);
        
        // Show the details panel by creating/updating the outer split pane
        if (outerSplit == null) {
            outerSplit = new SplitPane();
            outerSplit.getItems().addAll(mainSplit, issueDetailsPane);
            outerSplit.setDividerPositions(0.75);
            this.content.setCenter(outerSplit);
        } else {
            // If outer split already exists, just make sure details panel is visible
            if (!outerSplit.getItems().contains(issueDetailsPane)) {
                outerSplit.getItems().add(issueDetailsPane);
                outerSplit.setDividerPositions(0.75);
            }
        }
        
        // Navigate to line in code editor
        Platform.runLater(() -> {
            codeArea.moveTo(issue.getLine() - 1, 0);
            codeArea.selectLine();
            codeArea.requestFocus();
        });
    }
    
    private void hideIssueDetails() {
        // Remove the details panel from the split pane and restore main split only
        if (outerSplit != null && outerSplit.getItems().contains(issueDetailsPane)) {
            outerSplit.getItems().remove(issueDetailsPane);
            if (outerSplit.getItems().size() == 1) {
                // If only main split left, replace outer split with main split
                content.setCenter(mainSplit);
                outerSplit = null;
            }
        }
    }
    
    /**
     * Apply syntax highlighting to code issues based on their severity
     * @param result The analysis result containing issues to highlight
     */
    private void applyIssueHighlighting(CodeAnalysisResult result) {
        Platform.runLater(() -> {
            // Clear existing highlights
            codeArea.clearStyle(0, codeArea.getLength());
            
            // Apply base syntax highlighting first
            applySyntaxHighlighting();
            
            // Apply issue-based highlighting
            for (CodeIssue issue : result.getErrors()) {
                highlightIssueLine(issue);
            }
            for (CodeIssue issue : result.getSuggestions()) {
                highlightIssueLine(issue);
            }
        });
    }
    
    /**
     * Highlight a specific line based on issue severity
     * @param issue The issue to highlight
     */
    private void highlightIssueLine(CodeIssue issue) {
        try {
            int lineNumber = issue.getLine() - 1; // Convert to 0-based index
            if (lineNumber >= 0 && lineNumber < codeArea.getParagraphs().size()) {
                int startPos = codeArea.getAbsolutePosition(lineNumber, 0);
                int endPos = codeArea.getAbsolutePosition(lineNumber, codeArea.getParagraph(lineNumber).length());
                
                String severity = issue.getSeverity();
                String styleClass = getHighlightStyleClass(severity);
                
                // Apply the highlight style to the entire line
                codeArea.setStyleClass(startPos, endPos, styleClass);
            }
        } catch (Exception e) {
            logger.warning("Failed to highlight line " + issue.getLine() + ": " + e.getMessage());
        }
    }
    
    /**
     * Get CSS style class based on issue severity
     * @param severity The severity level
     * @return CSS style class name
     */
    private String getHighlightStyleClass(String severity) {
        if (severity == null) return "issue-info";
        
        switch (severity.toLowerCase()) {
            case "critical":
                return "issue-critical";
            case "warning":
                return "issue-warning";
            case "info":
                return "issue-info";
            default:
                return "issue-info";
        }
    }
    
    /**
     * Apply basic Java syntax highlighting
     */
    private void applySyntaxHighlighting() {
        // This is a basic implementation - could be enhanced with more sophisticated parsing
        String text = codeArea.getText();
        
        // Keywords
        highlightPattern(text, "\\b(public|private|protected|static|final|class|interface|extends|implements|import|package|return|if|else|for|while|try|catch|finally|throw|throws)\\b", "keyword");
        
        // String literals
        highlightPattern(text, "\"[^\"]*\"", "string");
        
        // Comments
        highlightPattern(text, "//.*$", "comment");
        highlightPattern(text, "/\\*[\\s\\S]*?\\*/", "comment");
        
        // Numbers
        highlightPattern(text, "\\b\\d+(\\.\\d+)?\\b", "number");
    }
    
    /**
     * Highlight text matching a regex pattern
     * @param text The full text to search
     * @param pattern The regex pattern to match
     * @param styleClass The CSS style class to apply
     */
    private void highlightPattern(String text, String pattern, String styleClass) {
        try {
            java.util.regex.Pattern regex = java.util.regex.Pattern.compile(pattern, java.util.regex.Pattern.MULTILINE);
            java.util.regex.Matcher matcher = regex.matcher(text);
            
            while (matcher.find()) {
                int start = matcher.start();
                int end = matcher.end();
                codeArea.setStyleClass(start, end, styleClass);
            }
        } catch (Exception e) {
            logger.warning("Failed to apply pattern highlighting: " + e.getMessage());
        }
    }
    
    private HBox createStatusBar() {
        HBox statusBar = new HBox(16);
        statusBar.getStyleClass().add("status-bar");
        statusBar.setPadding(new Insets(8, 16, 8, 16));
        statusBar.setAlignment(Pos.CENTER_LEFT);
        
        statusLabel = new Label("Ready");
        statusLabel.getStyleClass().add("status-label");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Label issueCountLabel = new Label("Issues: 0");
        issueCountLabel.getStyleClass().add("issue-count-label");
        
        statusBar.getChildren().addAll(statusLabel, spacer, issueCountLabel);
        return statusBar;
    }
    
    // Event Handlers
    private void openFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Java File");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Java Files", "*.java")
        );
        
        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            loadFile(file);
        }
    }
    
    private void loadFile(File file) {
        try {
            this.currentFile = file;
            filePathLabel.setText(file.getName());
            statusLabel.setText("Loading file...");
            
            // Load file content in background
            CompletableFuture.supplyAsync(() -> {
                try {
                    return java.nio.file.Files.readString(file.toPath());
                } catch (IOException e) {
                    logger.log(Level.SEVERE, "Error loading file", e);
                    return null;
                }
            }).thenAcceptAsync(content -> {
                if (content != null) {
                    codeArea.replaceText(content);
                    statusLabel.setText("File loaded: " + file.getName());
                    analyzeCode(); // Auto-analyze on file load
                } else {
                    statusLabel.setText("Error loading file");
                }
            }, Platform::runLater);
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error loading file", e);
            statusLabel.setText("Error: " + e.getMessage());
        }
    }
    
    private void saveFile() {
        if (currentFile != null) {
            try (FileWriter writer = new FileWriter(currentFile)) {
                writer.write(codeArea.getText());
                statusLabel.setText("File saved: " + currentFile.getName());
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Error saving file", e);
                statusLabel.setText("Error saving file");
            }
        }
    }
    
    private void analyzeCode() {
        if (codeArea.getText().trim().isEmpty()) {
            statusLabel.setText("No code to analyze");
            return;
        }
        
        analysisProgressBar.setVisible(true);
        statusLabel.setText("Analyzing code...");
        
        CompletableFuture.runAsync(() -> {
            controller.analyzeCode(codeArea.getText());
        }).thenRunAsync(() -> {
            // Analysis complete - controller should update the view
            analysisProgressBar.setVisible(false);
            statusLabel.setText("Analysis complete");
        }, Platform::runLater).exceptionally(throwable -> {
            analysisProgressBar.setVisible(false);
            statusLabel.setText("Analysis failed: " + throwable.getMessage());
            logger.log(Level.SEVERE, "Analysis error", throwable);
            return null;
        });
    }
    
    private void requestAiReview() {
        if (codeArea.getText().trim().isEmpty()) {
            statusLabel.setText("No code for AI review");
            return;
        }
        
        // Switch to AI tab
        analysisTabPane.getSelectionModel().select(3);
        
        aiProgressIndicator.setVisible(true);
        statusLabel.setText("Requesting AI review...");
        
        // Use the controller's method which now uses the API client
        String reviewPrompt = "Please provide a comprehensive code review";
        controller.onAskAiWithQuestion(codeArea.getText(), reviewPrompt);
        
        // Note: The response will be displayed in the main UI since the controller
        // now handles AI requests through the backend API
        aiProgressIndicator.setVisible(false);
        statusLabel.setText("AI review request sent - check main AI panel for response");
    }
    
    private void updateAnalysisResultsInternal(CodeAnalysisResult result) {
        if (result == null) {
            return;
        }
        
        // Clear previous results
        allIssues.clear();
        criticalIssues.clear();
        warnings.clear();
        suggestions.clear();
        
        // Categorize issues
        for (CodeIssue issue : result.getErrors()) {
            allIssues.add(issue);
            if ("critical".equalsIgnoreCase(issue.getSeverity())) {
                criticalIssues.add(issue);
            } else {
                warnings.add(issue);
            }
        }
        
        for (CodeIssue suggestion : result.getSuggestions()) {
            allIssues.add(suggestion);
            suggestions.add(suggestion);
        }
        
        // Update heatmap
        updateHeatmap();
        
        // Update tab titles with counts
        updateTabCounts();
    }
    
    private void updateTabCounts() {
        if (analysisTabPane != null) {
            analysisTabPane.getTabs().get(0).setText("🚨 Critical (" + criticalIssues.size() + ")");
            analysisTabPane.getTabs().get(1).setText("⚠️ Warnings (" + warnings.size() + ")");
            analysisTabPane.getTabs().get(2).setText("💡 Suggestions (" + suggestions.size() + ")");
        }
    }
    
    private void updateHeatmap() {
        heatmapPane.getChildren().clear();
        heatmapRectangles.clear();
        
        if (codeArea.getText().isEmpty()) {
            return;
        }
        
        String[] lines = codeArea.getText().split("\n");
        Map<Integer, Integer> issueCounts = new HashMap<>();
        
        // Count issues per line
        for (CodeIssue issue : allIssues) {
            issueCounts.merge(issue.getLine(), 1, Integer::sum);
        }
        
        // Create heatmap rectangles
        double rectHeight = Math.max(2, (double) heatmapPane.getHeight() / lines.length);
        
        for (int i = 1; i <= lines.length; i++) {
            Rectangle rect = new Rectangle(16, rectHeight);
            int issueCount = issueCounts.getOrDefault(i, 0);
            
            // Set color based on issue count
            if (issueCount == 0) {
                rect.setFill(Color.TRANSPARENT);
            } else if (issueCount == 1) {
                rect.setFill(Color.YELLOW);
            } else if (issueCount == 2) {
                rect.setFill(Color.ORANGE);
            } else {
                rect.setFill(Color.RED);
            }
            
            heatmapRectangles.put(i, rect);
            heatmapPane.getChildren().add(rect);
        }
    }
    
    private void onIssueClicked(MouseEvent event) {
        if (event.getClickCount() == 2) {
            @SuppressWarnings("unchecked")
            TableView<CodeIssue> table = (TableView<CodeIssue>) event.getSource();
            CodeIssue selectedIssue = table.getSelectionModel().getSelectedItem();
            
            if (selectedIssue != null) {
                // Show issue details in side panel
                showIssueDetails(selectedIssue);
            }
        }
    }
    
    private void updateIssueFilter() {
        String searchText = searchField.getText().toLowerCase();
        if (searchText.isEmpty()) {
            filteredIssues.setPredicate(null);
        } else {
            filteredIssues.setPredicate(issue -> 
                issue.getMessage().toLowerCase().contains(searchText) ||
                issue.getSeverity().toLowerCase().contains(searchText)
            );
        }
    }
    
    private void toggleHeatmap(boolean show) {
        heatmapPane.setVisible(show);
        heatmapPane.setManaged(show);
    }
    
    private void setupKeyboardShortcuts() {
        // Keyboard shortcuts will be set up when scene is created
    }
    
    // Public methods for MainController to call
    public void updateAnalysisResults(CodeAnalysisResult result) {
        Platform.runLater(() -> updateAnalysisResultsInternal(result));
    }
    
    public void displayError(String error) {
        Platform.runLater(() -> {
            statusLabel.setText("Error: " + error);
        });
    }
    
    public void displayMessage(String message) {
        Platform.runLater(() -> statusLabel.setText(message));
    }
    
    /**
     * Display analysis results in the modern UI
     * @param result The analysis result to display
     */
    public void displayAnalysisResult(CodeAnalysisResult result) {
        Platform.runLater(() -> {
            // Clear existing data
            allIssues.clear();
            criticalIssues.clear();
            warnings.clear();
            suggestions.clear();
            
            // Add all errors as issues
            allIssues.addAll(result.getErrors());
            
            // Categorize issues based on type or message content
            for (CodeIssue issue : result.getErrors()) {
                String message = issue.getMessage().toLowerCase();
                String severity = issue.getSeverity();
                
                if (message.contains("critical") || message.contains("security") || 
                    message.contains("vulnerability") || message.contains("sql injection") || 
                    message.contains("xss") || issue.getType() == CodeIssue.Type.ERROR) {
                    criticalIssues.add(issue);
                } else if (severity != null && severity.toLowerCase().contains("warning")) {
                    warnings.add(issue);
                } else {
                    suggestions.add(issue);
                }
            }
            
            // Add suggestions from result
            allIssues.addAll(result.getSuggestions());
            suggestions.addAll(result.getSuggestions());
            
            // Update status
            statusLabel.setText(String.format("Analysis complete: %d issues found", 
                result.getErrors().size() + result.getSuggestions().size()));
            
            // Update analysis summary
            updateAnalysisSummary(result);
            
            // Apply code highlighting for issues
            applyIssueHighlighting(result);
        });
    }
    
    /**
     * Display code in the editor
     * @param code The code to display
     */
    public void displayCode(String code) {
        Platform.runLater(() -> {
            codeArea.replaceText(code);
            // Clear any existing highlights when new code is loaded
            codeArea.clearStyle(0, codeArea.getLength());
            // Apply basic syntax highlighting
            applySyntaxHighlighting();
            statusLabel.setText("File loaded successfully");
        });
    }
    
    private void updateAnalysisSummary(CodeAnalysisResult result) {
        // Update status with summary information
        String summaryText = String.format(
            "Analysis complete: %d total issues (%d critical, %d warnings, %d suggestions)",
            result.getErrors().size() + result.getSuggestions().size(),
            criticalIssues.size(),
            warnings.size(),
            suggestions.size()
        );
        statusLabel.setText(summaryText);
    }
}
