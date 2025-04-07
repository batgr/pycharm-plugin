package com.github.batgr.pycharmplugin.widget.status;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.openapi.wm.StatusBarWidgetFactory;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class TypePanelFactory implements StatusBarWidgetFactory {

    private final String ID = "com.github.batgr.pycharmplugin.widget.status.typepanelfactory";
    @Override
    public @NotNull @NonNls String getId() {
        return ID;
    }

    @Override
    public @NotNull @NlsContexts.ConfigurableName String getDisplayName() {
        return "Python Type";
    }

    @Override
    public @NotNull StatusBarWidget createWidget(@NotNull Project project) {

        return new TypePanel(project);
    }



    @Override
    public void disposeWidget(@NotNull StatusBarWidget widget) {
        // Dispose the widget resources when the project closes
        Disposer.dispose(widget);
    }

    @Override
    public boolean canBeEnabledOn(@NotNull StatusBar statusBar) {
        // Allow the user to enable/disable this widget via the UI
        return true;
    }
}
