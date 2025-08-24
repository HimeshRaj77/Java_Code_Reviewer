package com.project.codereviewer.service;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.IntegerLiteralExpr;
import com.github.javaparser.ast.expr.DoubleLiteralExpr;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.body.Parameter;
import com.project.codereviewer.model.CodeAnalysisResult;
import com.project.codereviewer.model.CodeIssue;
import org.springframework.stereotype.Service;
import java.util.Optional;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

@Service
public class CodeAnalysisService {
    private static final Logger logger = Logger.getLogger(CodeAnalysisService.class.getName());
    
    private static final List<String> POOR_VARIABLE_NAMES = Arrays.asList(
        "temp", "tmp", "var", "x", "y", "z", "a", "b", "c", "foo", "bar",
        "data", "obj", "thing", "stuff", "item", "val", "value"
    );

    private boolean isPoorVariableName(String name) {
        if (name == null || name.length() < 2) return true;
        if (POOR_VARIABLE_NAMES.contains(name.toLowerCase())) return true;
        // Check for single letter followed by a number (e.g., a1, x2)
        if (name.length() == 2 && Character.isLetter(name.charAt(0)) && Character.isDigit(name.charAt(1))) return true;
        return false;
    }

    public CodeAnalysisResult analyze(String code) {
        if (code == null) {
            throw new IllegalArgumentException("Code cannot be null");
        }
        
        CodeAnalysisResult result = new CodeAnalysisResult();
        try {
            JavaParser parser = new JavaParser();
            var parseResult = parser.parse(code);
            if (parseResult.isSuccessful() && parseResult.getResult().isPresent()) {
                CompilationUnit cu = parseResult.getResult().get();
                for (MethodDeclaration method : cu.findAll(MethodDeclaration.class)) {
                    String methodName = method.getNameAsString();
                    int startLine = method.getBegin().map(b -> b.line).orElse(-1);
                    int length = method.getEnd().map(e -> e.line).orElse(-1) - startLine + 1;
                    int maxNesting = getMaxNestingEnhanced(method.getBody());
                    logger.fine("[DEBUG] Method: " + methodName + ", startLine: " + startLine + ", length: " + length + ", maxNesting: " + maxNesting);

                    if (length > 10) { // Lowered threshold
                        logger.fine("[DEBUG] Long method detected: " + methodName);
                        result.getErrors().add(new CodeIssue(startLine, methodName + ": Long method (" + length + " lines)", CodeIssue.Type.ERROR, "warning"));
                        result.getSuggestions().add(new CodeIssue(startLine, methodName + ": Consider refactoring into smaller methods for maintainability.", CodeIssue.Type.SUGGESTION, "info"));
                    }
                    if (maxNesting > 2) { // Lowered threshold
                        logger.fine("[DEBUG] Deep nesting detected: " + methodName);
                        result.getErrors().add(new CodeIssue(startLine, methodName + ": Deep nesting (" + maxNesting + ")", CodeIssue.Type.ERROR, "warning"));
                        result.getSuggestions().add(new CodeIssue(startLine, methodName + ": Try to reduce nesting by extracting logic or using guard clauses.", CodeIssue.Type.SUGGESTION, "info"));
                    }
                    if (method.getBody().isPresent() && method.getBody().get().isEmpty()) {
                        logger.fine("[DEBUG] Empty method detected: " + methodName);
                        result.getErrors().add(new CodeIssue(startLine, methodName + ": Empty method body", CodeIssue.Type.ERROR, "warning"));
                        result.getSuggestions().add(new CodeIssue(startLine, methodName + ": Remove or implement this method.", CodeIssue.Type.SUGGESTION, "info"));
                    }
                    
                    // Calculate cyclomatic complexity
                    AtomicInteger complexity = new AtomicInteger(1); // Base complexity
                    CyclomaticComplexityVisitor complexityVisitor = new CyclomaticComplexityVisitor();
                    if (method.getBody().isPresent()) {
                        method.getBody().get().accept(complexityVisitor, complexity);
                    }
                    int finalComplexity = complexity.get();
                    
                    if (finalComplexity > 5) { // Lowered threshold for better detection
                        result.getErrors().add(new CodeIssue(startLine, methodName + ": High cyclomatic complexity (" + finalComplexity + ")", CodeIssue.Type.ERROR, "warning"));
                        result.getSuggestions().add(new CodeIssue(startLine, methodName + ": Consider refactoring to reduce complexity. Current complexity: " + finalComplexity, CodeIssue.Type.SUGGESTION, "info"));
                    } else {
                        result.getSuggestions().add(new CodeIssue(startLine, methodName + ": Cyclomatic complexity: " + finalComplexity, CodeIssue.Type.SUGGESTION, "info"));
                    }

                    // Check for empty catch blocks
                    method.findAll(com.github.javaparser.ast.stmt.CatchClause.class).forEach(catchClause -> {
                        if (catchClause.getBody().getStatements().isEmpty()) {
                            int catchLine = catchClause.getBegin().get().line;
                            result.getErrors().add(new CodeIssue(catchLine, 
                                "Empty catch block found in " + methodName, 
                                CodeIssue.Type.ERROR, "warning"));
                        }
                    });

                    // Check for poor variable names
                    method.findAll(com.github.javaparser.ast.expr.VariableDeclarationExpr.class).forEach(varDecl -> {
                        varDecl.getVariables().forEach(var -> {
                            String varName = var.getNameAsString();
                            if (isPoorVariableName(varName)) {
                                int varLine = var.getBegin().get().line;
                                result.getErrors().add(new CodeIssue(varLine,
                                    "Poor variable name found: '" + varName + "'",
                                    CodeIssue.Type.ERROR, "warning"));
                            }
                        });
                    });
                }
                // Enhanced unused import detection using AST
                Set<String> usedTypes = new HashSet<>();
                TypeUsageVisitor typeVisitor = new TypeUsageVisitor();
                cu.accept(typeVisitor, usedTypes);
                
                long unusedImports = cu.getImports().stream()
                    .filter(imp -> {
                        String importName = imp.getNameAsString();
                        // Check if the imported type is actually used
                        return !usedTypes.contains(importName) && 
                               !usedTypes.contains(importName.substring(importName.lastIndexOf('.') + 1));
                    })
                    .count();
                logger.fine("[DEBUG] Unused imports count: " + unusedImports);
                if (unusedImports > 0) {
                    result.getErrors().add(new CodeIssue(1, "Unused imports detected: " + unusedImports, CodeIssue.Type.ERROR, "info"));
                    result.getSuggestions().add(new CodeIssue(1, "Remove unused imports to keep code clean.", CodeIssue.Type.SUGGESTION, "info"));
                }
                
                // Magic number detection
                MagicNumberVisitor magicNumberVisitor = new MagicNumberVisitor();
                cu.accept(magicNumberVisitor, result);
            } else {
                result.getErrors().add(new CodeIssue(0, "Error parsing code.", CodeIssue.Type.ERROR, "critical"));
            }
        } catch (Exception e) {
            result.getErrors().add(new CodeIssue(0, "Error analyzing code: " + e.getMessage(), CodeIssue.Type.ERROR, "critical"));
        }
        // Debug output
        logger.fine("[DEBUG] Detected errors:");
        for (CodeIssue err : result.getErrors()) {
            logger.fine("  - " + err.getMessage());
        }
        logger.fine("[DEBUG] Detected suggestions:");
        for (CodeIssue sug : result.getSuggestions()) {
            logger.fine("  - " + sug.getMessage());
        }
        return result;
    }

    private int getMaxNesting(Optional<BlockStmt> bodyOpt) {
        if (bodyOpt.isEmpty()) return 0;
        return getMaxNesting(bodyOpt.get(), 0);
    }

    private int getMaxNesting(BlockStmt block, int currentDepth) {
        int max = currentDepth;
        for (Statement stmt : block.getStatements()) {
            int childMax = max;
            if (stmt.isBlockStmt()) {
                childMax = getMaxNesting(stmt.asBlockStmt(), currentDepth + 1);
            } else if (stmt.isIfStmt() && stmt.asIfStmt().getThenStmt().isBlockStmt()) {
                childMax = getMaxNesting(stmt.asIfStmt().getThenStmt().asBlockStmt(), currentDepth + 1);
                if (stmt.asIfStmt().getElseStmt().isPresent() && stmt.asIfStmt().getElseStmt().get().isBlockStmt()) {
                    int elseMax = getMaxNesting(stmt.asIfStmt().getElseStmt().get().asBlockStmt(), currentDepth + 1);
                    childMax = Math.max(childMax, elseMax);
                }
            }
            max = Math.max(max, childMax);
        }
        return max;
    }
    
    /**
     * Enhanced nesting depth calculation using JavaParser visitor pattern
     */
    private int getMaxNestingEnhanced(Optional<BlockStmt> bodyOpt) {
        if (bodyOpt.isEmpty()) return 0;
        NestingDepthVisitor visitor = new NestingDepthVisitor();
        bodyOpt.get().accept(visitor, 0);
        return visitor.getMaxDepth();
    }
    
    /**
     * Visitor to calculate maximum nesting depth by traversing all control flow statements
     */
    private static class NestingDepthVisitor extends VoidVisitorAdapter<Integer> {
        private int maxDepth = 0;
        
        @Override
        public void visit(BlockStmt n, Integer depth) {
            maxDepth = Math.max(maxDepth, depth);
            super.visit(n, depth + 1);
        }
        
        @Override
        public void visit(com.github.javaparser.ast.stmt.IfStmt n, Integer depth) {
            maxDepth = Math.max(maxDepth, depth);
            super.visit(n, depth + 1);
        }
        
        @Override
        public void visit(com.github.javaparser.ast.stmt.ForStmt n, Integer depth) {
            maxDepth = Math.max(maxDepth, depth);
            super.visit(n, depth + 1);
        }
        
        @Override
        public void visit(com.github.javaparser.ast.stmt.WhileStmt n, Integer depth) {
            maxDepth = Math.max(maxDepth, depth);
            super.visit(n, depth + 1);
        }
        
        @Override
        public void visit(com.github.javaparser.ast.stmt.DoStmt n, Integer depth) {
            maxDepth = Math.max(maxDepth, depth);
            super.visit(n, depth + 1);
        }
        
        @Override
        public void visit(com.github.javaparser.ast.stmt.SwitchStmt n, Integer depth) {
            maxDepth = Math.max(maxDepth, depth);
            super.visit(n, depth + 1);
        }
        
        @Override
        public void visit(com.github.javaparser.ast.stmt.SwitchEntry n, Integer depth) {
            maxDepth = Math.max(maxDepth, depth);
            super.visit(n, depth + 1);
        }
        
        @Override
        public void visit(com.github.javaparser.ast.stmt.TryStmt n, Integer depth) {
            maxDepth = Math.max(maxDepth, depth);
            super.visit(n, depth + 1);
        }
        
        @Override
        public void visit(com.github.javaparser.ast.stmt.CatchClause n, Integer depth) {
            maxDepth = Math.max(maxDepth, depth);
            super.visit(n, depth + 1);
        }
        
        public int getMaxDepth() {
            return maxDepth;
        }
    }
    
    /**
     * Visitor to collect all used types from the AST
     */
    private static class TypeUsageVisitor extends VoidVisitorAdapter<Set<String>> {
        @Override
        public void visit(NameExpr n, Set<String> usedTypes) {
            usedTypes.add(n.getNameAsString());
            super.visit(n, usedTypes);
        }
        
        @Override
        public void visit(FieldAccessExpr n, Set<String> usedTypes) {
            usedTypes.add(n.getNameAsString());
            super.visit(n, usedTypes);
        }
        
        @Override
        public void visit(MethodCallExpr n, Set<String> usedTypes) {
            usedTypes.add(n.getNameAsString());
            super.visit(n, usedTypes);
        }
        
        @Override
        public void visit(ObjectCreationExpr n, Set<String> usedTypes) {
            usedTypes.add(n.getType().asString());
            super.visit(n, usedTypes);
        }
        
        @Override
        public void visit(VariableDeclarator n, Set<String> usedTypes) {
            usedTypes.add(n.getType().asString());
            super.visit(n, usedTypes);
        }
        
        @Override
        public void visit(Parameter n, Set<String> usedTypes) {
            usedTypes.add(n.getType().asString());
            super.visit(n, usedTypes);
        }
    }
    
    /**
     * Visitor to detect magic numbers in the code
     */
    private static class MagicNumberVisitor extends VoidVisitorAdapter<CodeAnalysisResult> {
        @Override
        public void visit(IntegerLiteralExpr n, CodeAnalysisResult result) {
            String value = n.getValue();
            int intValue = Integer.parseInt(value);
            
            // Ignore common numbers that are usually not magic
            if (intValue != 0 && intValue != 1 && intValue != -1) {
                int lineNumber = n.getBegin().map(b -> b.line).orElse(-1);
                if (lineNumber > 0) {
                    result.getSuggestions().add(new CodeIssue(lineNumber, 
                        "Magic number found: " + value + ". Consider refactoring into a named constant for better readability.", 
                        CodeIssue.Type.SUGGESTION, "info"));
                }
            }
            super.visit(n, result);
        }
        
        @Override
        public void visit(DoubleLiteralExpr n, CodeAnalysisResult result) {
            String value = n.getValue();
            double doubleValue = Double.parseDouble(value);
            
            // Ignore common numbers that are usually not magic
            if (doubleValue != 0.0 && doubleValue != 1.0 && doubleValue != -1.0) {
                int lineNumber = n.getBegin().map(b -> b.line).orElse(-1);
                if (lineNumber > 0) {
                    result.getSuggestions().add(new CodeIssue(lineNumber, 
                        "Magic number found: " + value + ". Consider refactoring into a named constant for better readability.", 
                        CodeIssue.Type.SUGGESTION, "info"));
                }
            }
            super.visit(n, result);
        }
    }
    
    /**
     * Visitor to calculate cyclomatic complexity by counting decision points
     */
    private static class CyclomaticComplexityVisitor extends VoidVisitorAdapter<AtomicInteger> {
        @Override
        public void visit(com.github.javaparser.ast.stmt.IfStmt n, AtomicInteger complexity) {
            complexity.incrementAndGet();
            super.visit(n, complexity);
        }
        
        @Override
        public void visit(com.github.javaparser.ast.stmt.ForStmt n, AtomicInteger complexity) {
            complexity.incrementAndGet();
            super.visit(n, complexity);
        }
        
        @Override
        public void visit(com.github.javaparser.ast.stmt.WhileStmt n, AtomicInteger complexity) {
            complexity.incrementAndGet();
            super.visit(n, complexity);
        }
        
        @Override
        public void visit(com.github.javaparser.ast.stmt.DoStmt n, AtomicInteger complexity) {
            complexity.incrementAndGet();
            super.visit(n, complexity);
        }
        
        @Override
        public void visit(com.github.javaparser.ast.stmt.CatchClause n, AtomicInteger complexity) {
            complexity.incrementAndGet();
            super.visit(n, complexity);
        }
        
        @Override
        public void visit(com.github.javaparser.ast.stmt.SwitchEntry n, AtomicInteger complexity) {
            complexity.incrementAndGet();
            super.visit(n, complexity);
        }
        
        @Override
        public void visit(BinaryExpr n, AtomicInteger complexity) {
            // Count logical operators as decision points
            if (n.getOperator() == BinaryExpr.Operator.AND || n.getOperator() == BinaryExpr.Operator.OR) {
                complexity.incrementAndGet();
            }
            super.visit(n, complexity);
        }
    }
} 