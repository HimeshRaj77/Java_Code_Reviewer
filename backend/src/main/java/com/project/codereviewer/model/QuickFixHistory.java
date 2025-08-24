package com.project.codereviewer.model;

import java.util.Stack;
import java.util.function.Consumer;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Manages the history of applied quick fixes, enabling undo/redo functionality.
 */
public class QuickFixHistory {
    private static final Logger logger = Logger.getLogger(QuickFixHistory.class.getName());
    
    private final Stack<CodeChange> undoStack = new Stack<>();
    private final Stack<CodeChange> redoStack = new Stack<>();
    private final Consumer<String> codeUpdater;
    
    public QuickFixHistory(Consumer<String> codeUpdater) {
        this.codeUpdater = codeUpdater;
    }
    
    /**
     * Records a code change and applies it.
     * @param oldCode The previous state of the code
     * @param newCode The new state of the code
     * @param description Description of the change
     */
    public void recordChange(String oldCode, String newCode, String description) {
        try {
            CodeChange change = new CodeChange(oldCode, newCode, description);
            undoStack.push(change);
            redoStack.clear(); // Clear redo stack when new change is made
            codeUpdater.accept(newCode);
            logger.fine("Recorded change: " + description);
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to record change", e);
        }
    }
    
    /**
     * Undoes the last quick fix.
     * @return true if undo was successful
     */
    public boolean undo() {
        if (undoStack.isEmpty()) {
            logger.fine("No changes to undo");
            return false;
        }
        
        try {
            CodeChange change = undoStack.pop();
            redoStack.push(change);
            codeUpdater.accept(change.oldCode);
            logger.fine("Undid change: " + change.description);
            return true;
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to undo change", e);
            return false;
        }
    }
    
    /**
     * Redoes the last undone quick fix.
     * @return true if redo was successful
     */
    public boolean redo() {
        if (redoStack.isEmpty()) {
            logger.fine("No changes to redo");
            return false;
        }
        
        try {
            CodeChange change = redoStack.pop();
            undoStack.push(change);
            codeUpdater.accept(change.newCode);
            logger.fine("Redid change: " + change.description);
            return true;
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to redo change", e);
            return false;
        }
    }
    
    /**
     * Checks if there are changes that can be undone.
     */
    public boolean canUndo() {
        return !undoStack.isEmpty();
    }
    
    /**
     * Checks if there are changes that can be redone.
     */
    public boolean canRedo() {
        return !redoStack.isEmpty();
    }
    
    /**
     * Gets the description of the next change that would be undone.
     */
    public String getUndoDescription() {
        return !undoStack.isEmpty() ? undoStack.peek().description : null;
    }
    
    /**
     * Gets the description of the next change that would be redone.
     */
    public String getRedoDescription() {
        return !redoStack.isEmpty() ? redoStack.peek().description : null;
    }
    
    private static class CodeChange {
        final String oldCode;
        final String newCode;
        final String description;
        
        CodeChange(String oldCode, String newCode, String description) {
            this.oldCode = oldCode;
            this.newCode = newCode;
            this.description = description;
        }
    }
}
