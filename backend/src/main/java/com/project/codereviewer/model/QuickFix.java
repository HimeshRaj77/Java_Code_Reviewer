package com.project.codereviewer.model;

import java.util.function.Consumer;

/**
 * Represents a quick fix that can be applied to code issues.
 * Quick fixes are automated code modifications that can resolve common issues.
 */
public interface QuickFix {
    /**
     * Gets the title of the quick fix to display in the UI.
     * @return A short, descriptive title of what the fix does
     */
    String getTitle();

    /**
     * Gets a detailed description of what the fix will do.
     * @return A description of the changes this fix will make
     */
    String getDescription();

    /**
     * Checks if this quick fix can be applied to the given issue.
     * @param issue The code issue to check
     * @return true if this fix can handle the given issue
     */
    boolean canFix(CodeIssue issue);

    /**
     * Applies the fix to the code.
     * @param issue The issue to fix
     * @param codeUpdater Consumer that receives the updated code
     * @param history Optional history tracker for undo/redo support
     * @return true if the fix was successfully applied
     */
    boolean apply(CodeIssue issue, Consumer<String> codeUpdater, QuickFixHistory history);
}
