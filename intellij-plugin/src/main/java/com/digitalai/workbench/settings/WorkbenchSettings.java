package com.digitalai.workbench.settings;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@State(
    name = "com.digitalai.workbench.settings.WorkbenchSettings",
    storages = @Storage("DigitalAiWorkbenchSettings.xml")
)
public class WorkbenchSettings implements PersistentStateComponent<WorkbenchSettings.State> {

    public static class State {
        public String marketplaceUrl = "https://github.com/Malli-Learn/ai-workbench.git";
        public String branch = "main";
        public String gitExecutable = "";
        public boolean autoRefresh = false;
    }

    private State myState = new State();

    public static WorkbenchSettings getInstance() {
        return ApplicationManager.getApplication().getService(WorkbenchSettings.class);
    }

    @Nullable
    @Override
    public State getState() {
        return myState;
    }

    @Override
    public void loadState(@NotNull State state) {
        myState = state;
    }

    public String getMarketplaceUrl() { return myState.marketplaceUrl; }
    public void setMarketplaceUrl(String url) { myState.marketplaceUrl = url; }
    
    public String getBranch() { return myState.branch; }
    public void setBranch(String branch) { myState.branch = branch; }
    
    public String getGitExecutable() { return myState.gitExecutable; }
    public void setGitExecutable(String gitExecutable) { myState.gitExecutable = gitExecutable; }
    
    public boolean isAutoRefresh() { return myState.autoRefresh; }
    public void setAutoRefresh(boolean autoRefresh) { myState.autoRefresh = autoRefresh; }
}
