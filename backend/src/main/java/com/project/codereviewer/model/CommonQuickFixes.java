package com.project.codereviewer.model;

import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Provides implementations of common quick fixes for Java code issues.
 * Each fix implementation follows these principles:
 * 1. Validates input before making changes
 * 2. Preserves code formatting and structure
 * 3. Handles edge cases gracefully
 * 4. Provides clear feedback on success/failure
 */
public class CommonQuickFixes {
    private static final Logger logger = Logger.getLogger(CommonQuickFixes.class.getName());
    
    // Common patterns for code analysis
    private static final Pattern IMPORT_PATTERN = Pattern.compile("^\\s*import\\s+([\\w\\.]+)\\s*;\\s*$");
    private static final Pattern METHOD_PATTERN = Pattern.compile("^\\s*(public|private|protected)?(\\s+static)?\\s+[\\w<>\\[\\]]+\\s+\\w+\\s*\\([^)]*\\)\\s*\\{?");
    
    /**
     * Creates a quick fix for removing unused imports.
     * This fix will:
     * 1. Validate that the line contains a proper import statement
     * 2. Remove only the specific unused import
     * 3. Preserve any comments and formatting
     */
    public static QuickFix unusedImportFix() {
        return new QuickFix() {
            @Override
            public String getTitle() {
                return "Remove Unused Import";
            }

            @Override
            public String getDescription() {
                return "Removes the unused import statement from the code.";
            }

            @Override
            public boolean canFix(CodeIssue issue) {
                try {
                    if (issue == null) {
                        logger.warning("Cannot fix: issue is null");
                        return false;
                    }
                    if (issue.getSourceCode() == null) {
                        logger.warning("Cannot fix: source code is null");
                        return false;
                    }
                    if (!issue.getMessage().toLowerCase().contains("unused import")) {
                        logger.fine("Not an unused import issue");
                        return false;
                    }
                    if (!IMPORT_PATTERN.matcher(issue.getLineContent()).matches()) {
                        logger.warning("Line does not match import pattern: " + issue.getLineContent());
                        return false;
                    }
                    return true;
                } catch (Exception e) {
                    logger.log(Level.WARNING, "Error in canFix", e);
                    return false;
                }
            }

            @Override
            public boolean apply(CodeIssue issue, Consumer<String> codeUpdater, QuickFixHistory history) {
                try {
                    logger.fine("Attempting to apply unused import fix");
                    String code = issue.getSourceCode();
                    if (code == null) {
                        logger.warning("Source code is null");
                        return false;
                    }

                    String[] lines = code.split("\n");
                    if (issue.getLineNumber() <= 0 || issue.getLineNumber() > lines.length) {
                        return false;
                    }

                    String importLine = lines[issue.getLineNumber() - 1];
                    // Verify this is actually an import statement
                    if (!IMPORT_PATTERN.matcher(importLine).matches()) {
                        return false;
                    }

                    StringBuilder newCode = new StringBuilder();
                    boolean preserveNextLineBreak = false;
                    
                    for (int i = 0; i < lines.length; i++) {
                        if (i + 1 == issue.getLineNumber()) {
                            // Skip the unused import line
                            // If this line is between other imports, we'll preserve formatting
                            boolean prevLineIsImport = i > 0 && IMPORT_PATTERN.matcher(lines[i - 1]).matches();
                            boolean nextLineIsImport = i < lines.length - 1 && IMPORT_PATTERN.matcher(lines[i + 1]).matches();
                            preserveNextLineBreak = prevLineIsImport && nextLineIsImport;
                            continue;
                        }
                        
                        newCode.append(lines[i]);
                        if (i < lines.length - 1 || preserveNextLineBreak) {
                            newCode.append("\n");
                        }
                    }
                    
                    String result = newCode.toString();
                    // Remove any double blank lines that might have been created
                    result = result.replaceAll("\n{3,}", "\n\n");
                    
                    if (history != null) {
                        history.recordChange(code, result, "Remove unused import: " + importLine.trim());
                    } else {
                        codeUpdater.accept(result);
                    }
                    return true;
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "Error applying unused import fix", e);
                    return false;
                }
            }
        };
    }

    /**
     * Creates a quick fix for extracting magic numbers into constants.
     */
    public static QuickFix magicNumberFix() {
        return new QuickFix() {
            private final Pattern numberPattern = Pattern.compile("\\b\\d+(\\.\\d+)?\\b");

            @Override
            public String getTitle() {
                return "Extract Magic Number to Constant";
            }

            @Override
            public String getDescription() {
                return "Extracts the magic number into a named constant at class level.";
            }

            @Override
            public boolean canFix(CodeIssue issue) {
                return issue.getMessage().toLowerCase().contains("magic number") &&
                       numberPattern.matcher(issue.getLineContent()).find();
            }

            @Override
            public boolean apply(CodeIssue issue, Consumer<String> codeUpdater, QuickFixHistory history) {
                String code = issue.getSourceCode();
                if (code == null) return false;

                String[] lines = code.split("\n");
                String lineWithNumber = lines[issue.getLineNumber() - 1];
                
                // Find the magic number in the line
                java.util.regex.Matcher matcher = numberPattern.matcher(lineWithNumber);
                if (!matcher.find()) return false;
                
                String number = matcher.group();
                String constantName = generateConstantName(number, lineWithNumber);
                
                // Create the constant declaration
                StringBuilder newCode = new StringBuilder();
                boolean addedConstant = false;
                
                // Find the class declaration and add constant after it
                for (int i = 0; i < lines.length; i++) {
                    newCode.append(lines[i]).append("\n");
                    if (!addedConstant && lines[i].contains("class")) {
                        newCode.append("    private static final ")
                              .append(number.contains(".") ? "double" : "int")
                              .append(" ").append(constantName)
                              .append(" = ").append(number).append(";\n\n");
                        addedConstant = true;
                    }
                }
                
                // Replace the magic number with the constant
                String updatedLine = lineWithNumber.replace(number, constantName);
                String result = newCode.toString().replace(lineWithNumber, updatedLine);

                if (history != null) {
                    history.recordChange(code, result, "Extract magic number " + number + " to constant " + constantName);
                } else {
                    codeUpdater.accept(result);
                }
                return true;
            }
            
            private String generateConstantName(String number, String context) {
                // Try to generate a meaningful name based on the context
                String[] words = context.toLowerCase().split("[^a-zA-Z]+");
                StringBuilder name = new StringBuilder();
                
                for (String word : words) {
                    if (!word.isEmpty() && !word.equals("final") && !word.equals("new")) {
                        name.append(word.substring(0, 1).toUpperCase())
                            .append(word.substring(1));
                    }
                }
                
                return name.append("Value").toString().toUpperCase();
            }
        };
    }

    /**
     * Creates a quick fix for splitting long methods.
     */
    public static QuickFix longMethodFix() {
        return new QuickFix() {
            @Override
            public String getTitle() {
                return "Split Method";
            }

            @Override
            public String getDescription() {
                return "Suggests a split point for the long method based on logical blocks.";
            }

            @Override
            public boolean canFix(CodeIssue issue) {
                return issue.getMessage().toLowerCase().contains("method is too long");
            }

            @Override
            public boolean apply(CodeIssue issue, Consumer<String> codeUpdater, QuickFixHistory history) {
                String code = issue.getSourceCode();
                if (code == null) return false;

                String[] lines = code.split("\n");
                int methodStart = -1;
                int methodEnd = -1;
                int bracketCount = 0;
                boolean inMethod = false;

                // Find the method boundaries
                for (int i = 0; i < lines.length; i++) {
                    String line = lines[i].trim();
                    if (!inMethod && METHOD_PATTERN.matcher(lines[i]).matches() && i + 1 == issue.getLineNumber()) {
                        methodStart = i;
                        inMethod = true;
                        bracketCount++;
                        continue;
                    }
                    if (inMethod) {
                        bracketCount += countChar(line, '{');
                        bracketCount -= countChar(line, '}');
                        if (bracketCount == 0) {
                            methodEnd = i;
                            break;
                        }
                    }
                }

                if (methodStart == -1 || methodEnd == -1) return false;

                // Analyze the method and find logical splitting points
                StringBuilder newCode = new StringBuilder();
                LogicalBlockAnalyzer analyzer = new LogicalBlockAnalyzer();
                
                // Add comments for each suggested split
                for (int i = 0; i < lines.length; i++) {
                    if (i == methodStart) {
                        // Add refactoring suggestions as comments
                        newCode.append("    // TODO: Consider refactoring this method into smaller methods:\n");
                        for (String suggestion : analyzer.analyzeLongMethod(lines, methodStart, methodEnd)) {
                            newCode.append("    // - ").append(suggestion).append("\n");
                        }
                        newCode.append("\n");
                    }
                    newCode.append(lines[i]).append("\n");
                }

                String result = newCode.toString();
                if (history != null) {
                    history.recordChange(code, result, "Add method splitting suggestions to long method");
                } else {
                    codeUpdater.accept(result);
                }
                return true;
            }

            private int countChar(String str, char ch) {
                return (int) str.chars().filter(c -> c == ch).count();
            }
        };
    }

    /**
     * Helper class to analyze method structure and suggest logical splitting points
     */
    private static class LogicalBlockAnalyzer {
        private static final Pattern LOOP_PATTERN = Pattern.compile("^\\s*(for|while|do)\\b");
        private static final Pattern IF_PATTERN = Pattern.compile("^\\s*(if|else|else\\s+if)\\b");
        private static final Pattern TRY_PATTERN = Pattern.compile("^\\s*(try|catch|finally)\\b");

        public List<String> analyzeLongMethod(String[] lines, int start, int end) {
            List<String> suggestions = new ArrayList<>();
            List<CodeBlock> blocks = identifyLogicalBlocks(lines, start, end);
            
            // Analyze blocks and generate suggestions
            for (CodeBlock block : blocks) {
                if (block.complexity > 2 || block.lineCount > 10) {
                    String name = generateMethodName(block);
                    suggestions.add("Extract " + block.type + " block into method '" + name + 
                                 "' (lines " + block.startLine + "-" + block.endLine + ")");
                }
            }
            
            return suggestions;
        }

        private List<CodeBlock> identifyLogicalBlocks(String[] lines, int start, int end) {
            List<CodeBlock> blocks = new ArrayList<>();
            CodeBlock currentBlock = null;

            for (int i = start; i <= end; i++) {
                String line = lines[i].trim();
                
                // Check for start of new logical block
                if (LOOP_PATTERN.matcher(line).find()) {
                    currentBlock = new CodeBlock("loop", i);
                } else if (IF_PATTERN.matcher(line).find()) {
                    currentBlock = new CodeBlock("conditional", i);
                } else if (TRY_PATTERN.matcher(line).find()) {
                    currentBlock = new CodeBlock("error handling", i);
                } else if (line.contains("private") || line.contains("public") || line.contains("protected")) {
                    currentBlock = new CodeBlock("method", i);
                }

                // Update block info
                if (currentBlock != null) {
                    currentBlock.lineCount++;
                    if (line.contains("if") || line.contains("for") || line.contains("while") || 
                        line.contains("catch") || line.contains("case")) {
                        currentBlock.complexity++;
                    }

                    // Check for block end
                    if (line.contains("}") && currentBlock.braceCount == 0) {
                        currentBlock.endLine = i;
                        blocks.add(currentBlock);
                        currentBlock = null;
                    } else {
                        currentBlock.braceCount += countBraces(line);
                    }
                }
            }

            return blocks;
        }

        private String generateMethodName(CodeBlock block) {
            String prefix = switch (block.type) {
                case "loop" -> "process";
                case "conditional" -> "handle";
                case "error handling" -> "handle";
                default -> "execute";
            };
            return prefix + "Block" + block.startLine;
        }

        private int countBraces(String line) {
            return (int) line.chars().filter(ch -> ch == '{').count() -
                   (int) line.chars().filter(ch -> ch == '}').count();
        }

        private static class CodeBlock {
            String type;
            int startLine;
            int endLine;
            int lineCount;
            int complexity;
            int braceCount;

            CodeBlock(String type, int startLine) {
                this.type = type;
                this.startLine = startLine;
                this.lineCount = 0;
                this.complexity = 0;
                this.braceCount = 0;
            }
        }
    }

    /**
     * Creates a quick fix for empty catch blocks.
     */
    public static QuickFix emptyCatchBlockFix() {
        return new QuickFix() {
            private static final Pattern CATCH_PATTERN = Pattern.compile("^\\s*catch\\s*\\([^)]+\\)\\s*\\{\\s*}\\s*$");
            
            @Override
            public String getTitle() {
                return "Fix Empty Catch Block";
            }

            @Override
            public String getDescription() {
                return "Adds appropriate error logging to empty catch block.";
            }

            @Override
            public boolean canFix(CodeIssue issue) {
                return issue.getMessage().toLowerCase().contains("empty catch block") &&
                       issue.getLineContent() != null &&
                       CATCH_PATTERN.matcher(issue.getLineContent()).matches();
            }

            @Override
            public boolean apply(CodeIssue issue, Consumer<String> codeUpdater, QuickFixHistory history) {
                String code = issue.getSourceCode();
                if (code == null) return false;

                String[] lines = code.split("\n");
                if (issue.getLineNumber() <= 0 || issue.getLineNumber() > lines.length) {
                    return false;
                }

                // Extract exception variable name from catch declaration
                String catchLine = lines[issue.getLineNumber() - 1];
                String exceptionVar = extractExceptionVar(catchLine);
                if (exceptionVar == null) return false;

                StringBuilder newCode = new StringBuilder();
                for (int i = 0; i < lines.length; i++) {
                    newCode.append(lines[i]);
                    if (i + 1 == issue.getLineNumber()) {
                        // Replace empty catch block with proper logging
                        newCode.append("\n            logger.warning(\"Exception caught: \" + ")
                              .append(exceptionVar).append(".getMessage());")
                              .append("\n            logger.fine(\"Stack trace:\", ")
                              .append(exceptionVar).append(");");
                    }
                    newCode.append("\n");
                }

                String result = newCode.toString();
                if (history != null) {
                    history.recordChange(code, result, "Add logging to empty catch block");
                } else {
                    codeUpdater.accept(result);
                }
                return true;
            }

            private String extractExceptionVar(String catchLine) {
                int start = catchLine.indexOf('(');
                int end = catchLine.indexOf(')');
                if (start == -1 || end == -1) return null;
                
                String[] parts = catchLine.substring(start + 1, end).trim().split("\\s+");
                return parts.length >= 2 ? parts[1] : null;
            }
        };
    }

    /**
     * Creates a quick fix for poorly named variables.
     */
    public static QuickFix poorVariableNameFix() {
        return new QuickFix() {
            private static final Pattern VAR_PATTERN = Pattern.compile("\\b(var|[a-zA-Z_$][a-zA-Z0-9_$]*)\\s+([a-z][a-zA-Z0-9_$]*)\\s*[=;]");
            private static final List<String> POOR_NAMES = List.of("temp", "tmp", "var", "x", "y", "z", "a", "b", "c", "foo", "bar");
            
            @Override
            public String getTitle() {
                return "Improve Variable Name";
            }

            @Override
            public String getDescription() {
                return "Suggests a more descriptive name for the variable based on its usage and context.";
            }

            @Override
            public boolean canFix(CodeIssue issue) {
                if (issue.getLineContent() == null) return false;
                
                java.util.regex.Matcher matcher = VAR_PATTERN.matcher(issue.getLineContent());
                return issue.getMessage().toLowerCase().contains("variable name") &&
                       matcher.find() &&
                       POOR_NAMES.contains(matcher.group(2).toLowerCase());
            }

            @Override
            public boolean apply(CodeIssue issue, Consumer<String> codeUpdater, QuickFixHistory history) {
                String code = issue.getSourceCode();
                if (code == null) return false;

                String[] lines = code.split("\n");
                String line = lines[issue.getLineNumber() - 1];
                
                java.util.regex.Matcher matcher = VAR_PATTERN.matcher(line);
                if (!matcher.find()) return false;

                String oldName = matcher.group(2);
                String type = matcher.group(1);
                String newName = suggestVariableName(type, line, oldName);

                StringBuilder newCode = new StringBuilder();
                boolean addedSuggestion = false;
                
                for (int i = 0; i < lines.length; i++) {
                    if (i + 1 == issue.getLineNumber()) {
                        // Add suggestion as comment
                        newCode.append("    // TODO: Consider renaming '")
                              .append(oldName).append("' to '")
                              .append(newName).append("' for better clarity\n");
                        addedSuggestion = true;
                    }
                    newCode.append(lines[i]).append("\n");
                }

                if (!addedSuggestion) return false;

                String result = newCode.toString();
                if (history != null) {
                    history.recordChange(code, result, "Add variable rename suggestion from '" + oldName + "' to '" + newName + "'");
                } else {
                    codeUpdater.accept(result);
                }
                return true;
            }

            private String suggestVariableName(String type, String context, String oldName) {
                // Try to infer meaning from assignment
                String[] parts = context.split("[=;]");
                if (parts.length > 1) {
                    String rhs = parts[1].trim();
                    if (rhs.contains("new ")) {
                        String className = rhs.substring(rhs.indexOf("new ") + 4).split("[(<\\s]")[0];
                        return toLowerCamelCase(className);
                    }
                    if (rhs.contains(".")) {
                        String methodName = rhs.substring(rhs.lastIndexOf(".") + 1).split("[(<\\s]")[0];
                        return toLowerCamelCase(methodName + "Result");
                    }
                }

                // Use type information
                if (type.equals("String")) return "text";
                if (type.equals("int") || type.equals("long")) return "count";
                if (type.equals("boolean")) return "isValid";
                if (type.contains("List")) return "items";
                if (type.contains("Map")) return "mappings";

                // Fallback to generic but descriptive name
                return "processed" + toTitleCase(oldName);
            }

            private String toLowerCamelCase(String s) {
                if (s == null || s.isEmpty()) return "value";
                return Character.toLowerCase(s.charAt(0)) + s.substring(1);
            }

            private String toTitleCase(String s) {
                if (s == null || s.isEmpty()) return "Value";
                return Character.toUpperCase(s.charAt(0)) + s.substring(1);
            }
        };
    }
}
