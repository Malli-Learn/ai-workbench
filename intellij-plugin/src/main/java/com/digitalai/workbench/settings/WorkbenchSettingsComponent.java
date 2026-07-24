package com.digitalai.workbench.settings;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import com.digitalai.workbench.catalog.GitCatalogRepository;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class WorkbenchSettingsComponent {
    private static final Logger LOG = Logger.getInstance(WorkbenchSettingsComponent.class);

    private final JPanel myMainPanel;
    private final JBTextField myMarketplaceUrlText = new JBTextField();
    private final JBTextField myBranchText = new JBTextField();
    private final JBTextField myGitExecutableText = new JBTextField();
    private final JBCheckBox myAutoRefreshCheckBox = new JBCheckBox("Automatically refresh catalog when the tool window opens");
    private final JButton myTestConnectionButton = new JButton("Test Connection");
    private final JButton myClearCacheButton = new JButton("Clear Catalog Cache");

    public WorkbenchSettingsComponent() {
        myTestConnectionButton.addActionListener(this::testConnection);
        myClearCacheButton.addActionListener(this::clearCache);

        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.X_AXIS));
        buttonsPanel.add(myTestConnectionButton);
        buttonsPanel.add(Box.createHorizontalStrut(10));
        buttonsPanel.add(myClearCacheButton);

        myMainPanel = FormBuilder.createFormBuilder()
            .addLabeledComponent(new JBLabel("Marketplace Git URL:"), myMarketplaceUrlText, 1, false)
            .addLabeledComponent(new JBLabel("Branch or tag:"), myBranchText, 1, false)
            .addLabeledComponent(new JBLabel("Git executable path (optional):"), myGitExecutableText, 1, false)
            .addComponent(myAutoRefreshCheckBox, 1)
            .addComponent(buttonsPanel, 1)
            .addComponentFillVertically(new JPanel(), 0)
            .getPanel();
    }

    public JPanel getPanel() {
        return myMainPanel;
    }

    public JComponent getPreferredFocusedComponent() {
        return myMarketplaceUrlText;
    }

    public String getMarketplaceUrl() { return myMarketplaceUrlText.getText(); }
    public void setMarketplaceUrl(String newText) { myMarketplaceUrlText.setText(newText); }

    public String getBranch() { return myBranchText.getText(); }
    public void setBranch(String newText) { myBranchText.setText(newText); }

    public String getGitExecutable() { return myGitExecutableText.getText(); }
    public void setGitExecutable(String newText) { myGitExecutableText.setText(newText); }

    public boolean getAutoRefresh() { return myAutoRefreshCheckBox.isSelected(); }
    public void setAutoRefresh(boolean selected) { myAutoRefreshCheckBox.setSelected(selected); }

    private void testConnection(ActionEvent e) {
        String url = getMarketplaceUrl();
        String branch = getBranch();
        
        ProgressManager.getInstance().run(new Task.Modal(null, "Testing Git Connection", true) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                try {
                    GitCatalogRepository repo = new GitCatalogRepository();
                    boolean success = repo.testConnection(url, branch, getGitExecutable());
                    ApplicationManager.getApplication().invokeLater(() -> {
                        if (success) {
                            Messages.showInfoMessage("Connection successful.", "Connection Test");
                        } else {
                            showConnectionError(url);
                        }
                    });
                } catch (Exception ex) {
                    LOG.warn("Connection test failed", ex);
                    ApplicationManager.getApplication().invokeLater(() -> showConnectionError(url));
                }
            }
        });
    }

    private void showConnectionError(String url) {
        Messages.showErrorDialog(
            "Unable to access the marketplace repository.\n\n" +
            "Verify access by running:\n" +
            "git ls-remote " + url + "\n\n" +
            "Configure Git credentials, enterprise SSO, VPN, and certificate trust before retrying.",
            "Connection Test Failed"
        );
    }

    private void clearCache(ActionEvent e) {
        try {
            GitCatalogRepository repo = new GitCatalogRepository();
            repo.clearCache();
            Messages.showInfoMessage("Catalog cache cleared successfully.", "Clear Cache");
        } catch (Exception ex) {
            LOG.warn("Failed to clear cache", ex);
            Messages.showErrorDialog("Failed to clear cache: " + ex.getMessage(), "Error");
        }
    }
}
