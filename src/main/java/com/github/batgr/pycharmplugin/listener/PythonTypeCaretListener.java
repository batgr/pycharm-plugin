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
        Editor editor = event.getEditor();
        Project project = editor.getProject();
        if (project == null || project.isDisposed()) {
            return;
        }

        PsiDocumentManager.getInstance(project).commitDocument(editor.getDocument());
        PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument());

        if (psiFile == null) {
            updateTypeInfo("", "", project);
            return;
        }

        if (!(psiFile instanceof PyFile)) {
            updateTypeInfo("", "", project);
            return;
        }

        int offset = editor.getCaretModel().getOffset();
        PsiElement element = psiFile.findElementAt(offset);

        if (element == null) {
            updateTypeInfo("", "", project);
            return;
        }


        PyReferenceExpression refExpr = PsiTreeUtil.getParentOfType(element, PyReferenceExpression.class);
        if (refExpr != null) {
            evaluateAndUpdateType(refExpr, project, psiFile);
            return;
        }


        PyTypedElement typedElement = PsiTreeUtil.getParentOfType(element, PyTypedElement.class);
        if (typedElement != null) {
            evaluateAndUpdateType(typedElement, project, psiFile);
            return;
        }

        updateTypeInfo("", "", project);
    }

    private void evaluateAndUpdateType(@NotNull PyTypedElement element, @NotNull Project project, @NotNull PsiFile file) {
        ReadAction.nonBlocking(()->{
            TypeEvalContext context = TypeEvalContext.codeAnalysis(project, file);
            PyType type = context.getType(element);
            String name = element.getName();
            String typeName = (type != null) ? type.getName() : "Any";
            return new Pair<>(typeName, name);
        }).expireWhen(project::isDisposed).finishOnUiThread(ModalityState.defaultModalityState(), (result) -> {
            updateTypeInfo(result.getFirst(), result.getSecond(), project);
        }).submit(AppExecutorUtil.getAppExecutorService());

    }

    private void updateTypeInfo(@Nullable String typeName, @Nullable String name, @NotNull Project project) {
        ApplicationManager.getApplication().invokeLater(() -> {
            StatusBar statusBar = WindowManager.getInstance().getStatusBar(project);
            if (statusBar != null) {
                StatusBarWidget widget = statusBar.getWidget(TypePanel.ID);
                if (widget instanceof TypePanel pyWidget) {
                    pyWidget.updateText(project, typeName, name);
                }
            }
        });
    }


}

