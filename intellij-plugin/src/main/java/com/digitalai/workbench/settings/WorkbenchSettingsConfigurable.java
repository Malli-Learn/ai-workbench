package com.digitalai.workbench.settings;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.util.NlsContexts;
import org.jetbrains.annotations.Nullable;
import javax.swing.JComponent;
import java.util.Objects;

public class WorkbenchSettingsConfigurable implements Configurable {

    private WorkbenchSettingsComponent mySettingsComponent;

    @NlsContexts.ConfigurableName
    @Override
    public String getDisplayName() {
        return "Digital AI Workbench";
    }

    @Override
    public @Nullable JComponent createComponent() {
        mySettingsComponent = new WorkbenchSettingsComponent();
        return mySettingsComponent.getPanel();
    }

    @Override
    public boolean isModified() {
        WorkbenchSettings.State state = WorkbenchSettings.getInstance().getState();
        if (state == null) return false;
        
        boolean modified = !mySettingsComponent.getMarketplaceUrl().equals(state.marketplaceUrl);
        modified |= !mySettingsComponent.getBranch().equals(state.branch);
        modified |= !mySettingsComponent.getGitExecutable().equals(state.gitExecutable);
        modified |= mySettingsComponent.getAutoRefresh() != state.autoRefresh;
        return modified;
    }

    @Override
    public void apply() {
        WorkbenchSettings.State state = WorkbenchSettings.getInstance().getState();
        if (state != null) {
            state.marketplaceUrl = mySettingsComponent.getMarketplaceUrl();
            state.branch = mySettingsComponent.getBranch();
            state.gitExecutable = mySettingsComponent.getGitExecutable();
            state.autoRefresh = mySettingsComponent.getAutoRefresh();
        }
    }

    @Override
    public void reset() {
        WorkbenchSettings.State state = WorkbenchSettings.getInstance().getState();
        if (state != null) {
            mySettingsComponent.setMarketplaceUrl(state.marketplaceUrl);
            mySettingsComponent.setBranch(state.branch);
            mySettingsComponent.setGitExecutable(state.gitExecutable);
            mySettingsComponent.setAutoRefresh(state.autoRefresh);
        }
    }

    @Override
    public void disposeUIResources() {
        mySettingsComponent = null;
    }
}
