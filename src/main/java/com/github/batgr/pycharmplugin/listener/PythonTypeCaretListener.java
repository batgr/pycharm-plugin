package com.github.batgr.pycharmplugin.listener;

import com.github.batgr.pycharmplugin.widget.status.TypePanel;


import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.event.CaretEvent;
import com.intellij.openapi.editor.event.CaretListener;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;

import com.intellij.util.concurrency.AppExecutorUtil;
import com.jetbrains.python.psi.PyFile;
import com.jetbrains.python.psi.PyReferenceExpression;
import com.jetbrains.python.psi.PyTypedElement;
import com.jetbrains.python.psi.types.PyType;
import com.jetbrains.python.psi.types.TypeEvalContext;
import kotlin.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PythonTypeCaretListener implements CaretListener {




    @Override
    public void caretPositionChanged(@NotNull CaretEvent event) {

        // Get the editor and project
        Editor editor = event.getEditor();
        Project project = editor.getProject();
        if (project == null || project.isDisposed()) {
            return;
        }

        // Commit any pending document changes to ensure PSI is up-to-date
        PsiDocumentManager.getInstance(project).commitDocument(editor.getDocument());

        // Get the PSI file associated with the editor
        PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument());

        if (psiFile == null) {
            updateTypeInfo("", "", project);
            return;
        }

        // Only process Python files
        if (!(psiFile instanceof PyFile)) {
            updateTypeInfo("", "", project);
            return;
        }

        // Get the PSI element at the caret position
        int offset = editor.getCaretModel().getOffset();
        PsiElement element = psiFile.findElementAt(offset);

        if (element == null) {
            updateTypeInfo("", "", project);
            return;
        }

        // Check if element is part of a Python reference expression (e.g., variable or function call)
        PyReferenceExpression refExpr = PsiTreeUtil.getParentOfType(element, PyReferenceExpression.class);
        if (refExpr != null) {
            evaluateAndUpdateType(refExpr, project, psiFile);
            return;
        }

        // If not a reference, check if it's any Python typed element
        PyTypedElement typedElement = PsiTreeUtil.getParentOfType(element, PyTypedElement.class);
        if (typedElement != null) {
            evaluateAndUpdateType(typedElement, project, psiFile);
            return;
        }

        // Clear type info if no typed element is found
        updateTypeInfo("", "", project);
    }

    private void evaluateAndUpdateType(@NotNull PyTypedElement element, @NotNull Project project, @NotNull PsiFile file) {

        // Use non-blocking read action to evaluate type in the background
        ReadAction.nonBlocking(()->{
            // Create a type evaluation context for code analysis
            TypeEvalContext context = TypeEvalContext.codeAnalysis(project, file);
            // Get the element's type and name
            PyType type = context.getType(element);
            String name = element.getName()==null?"Unknown":element.getName();
            String typeName = (type != null) ? type.getName() : "Any";

            return new Pair<>(typeName, name);
        })
                .expireWhen(project::isDisposed) // Cancel the operation if the project is disposed
                .finishOnUiThread(ModalityState.defaultModalityState(), (result) -> {
            updateTypeInfo(result.getFirst(), result.getSecond(), project);
        }).submit(AppExecutorUtil.getAppExecutorService()); // Submit the task to the application thread pool

    }

    private void updateTypeInfo(@Nullable String typeName, @Nullable String name, @NotNull Project project) {

        // Invoke on EDT to update UI components
        ApplicationManager.getApplication().invokeLater(() -> {
            // Get the status bar for the project
            StatusBar statusBar = WindowManager.getInstance().getStatusBar(project);
            if (statusBar != null) {
                // Find our custom status bar widget
                StatusBarWidget widget = statusBar.getWidget(TypePanel.ID);
                if (widget instanceof TypePanel pyWidget) {
                    // Update the text in the widget
                    pyWidget.updateText(project, typeName, name);
                }
            }
        });
    }


}

