package com.github.batgr.pythonplugin.listener;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.event.CaretListener;
import com.intellij.openapi.editor.event.EditorFactoryEvent;
import com.intellij.openapi.editor.event.EditorFactoryListener;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

public class PythonEditorFactoryListener implements EditorFactoryListener {

    @Override
    public void editorCreated(@NotNull EditorFactoryEvent event) {
        Editor editor = event.getEditor();
        VirtualFile file = FileDocumentManager.getInstance().getFile(editor.getDocument());

        // Only add listener for Python files
        if (file != null && "py".equals(file.getExtension())) {
            CaretListener caretListener = new PythonTypeCaretListener();
            editor.getCaretModel().addCaretListener(caretListener);
        }
    }
}
