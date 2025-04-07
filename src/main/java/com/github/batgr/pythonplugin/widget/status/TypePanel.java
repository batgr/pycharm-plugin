package com.github.batgr.pythonplugin.widget.status;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.util.Consumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.MouseEvent;

public class TypePanel implements StatusBarWidget,StatusBarWidget.TextPresentation{
    public  static final String ID = "TypePanel";
    private final Project project;
    private String currentText;
    private StatusBar statusBar;

    public TypePanel(Project project) {
        this.project = project;
        currentText = "";
    }


    @Override
    public @NotNull String ID() {
        return ID;
    }
    @Override
    public @NotNull StatusBarWidget.WidgetPresentation getPresentation() {
        return this;
    }

    @Override
    public @NotNull String getText() {
        return currentText;
    }

    @Override
    public float getAlignment() {
        return JComponent.CENTER_ALIGNMENT;
    }



    @Override
    public void install(@NotNull StatusBar statusBar) {
        this.statusBar = statusBar;
    }

    @Override
    public void dispose() {
        // Clean up resources if any
        this.statusBar = null;
    }





    @Override
    public @Nullable @NlsContexts.Tooltip String getTooltipText() {
        // Tooltip shown on hover
        return "Inferred python type";
    }

    @Override
    public @Nullable Consumer<MouseEvent> getClickConsumer() {
        // No action on click
        return null;
    }

    public void updateText(@NotNull Project project, @Nullable String text,@Nullable String name) {
        // Ensure update happens only for the relevant project
        if (!this.project.equals(project)) {
            return;
        }
        this.currentText = (text == null || text.trim().isEmpty()) ? "" : name+": " + text;
        if (this.statusBar != null) {
            // Request the status bar to repaint this widget
            this.statusBar.updateWidget(ID());
        }
    }
}
