package com.digitalai.workbench.ui;

import com.digitalai.workbench.catalog.GitCatalogRepository;
import com.digitalai.workbench.catalog.MarketplaceParser;
import com.digitalai.workbench.catalog.MarketplaceValidator;
import com.digitalai.workbench.catalog.model.MarketplaceManifest;
import com.digitalai.workbench.catalog.model.MarketplacePluginEntry;
import com.digitalai.workbench.catalog.model.PluginManifest;
import com.digitalai.workbench.install.InstallationRegistry;
import com.digitalai.workbench.install.InstallationRegistryService;
import com.digitalai.workbench.install.PersonalPluginInstaller;
import com.digitalai.workbench.settings.WorkbenchSettings;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.SearchTextField;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.List;

public class WorkbenchPanel {
    private static final Logger LOG = Logger.getInstance(WorkbenchPanel.class);
    private final Project project;
    private final JPanel mainPanel;
    private final JPanel pluginListPanel;
    private final SearchTextField searchField;
    private final JComboBox<String> categoryFilter;
    private final JCheckBox installedFilter;
    private final JLabel statusLabel;
    private final JLabel emptyStateLabel;
    
    private final GitCatalogRepository catalogRepo = new GitCatalogRepository();
    private final MarketplaceParser parser = new MarketplaceParser();
    private final MarketplaceValidator validator = new MarketplaceValidator();
    private final PersonalPluginInstaller installer = new PersonalPluginInstaller();
    private final InstallationRegistryService registryService = InstallationRegistryService.getInstance();
    
    private MarketplaceManifest currentManifest;
    private File currentCatalogDir;

    public WorkbenchPanel(Project project) {
        this.project = project;
        this.mainPanel = new JPanel(new BorderLayout());
        this.pluginListPanel = new JPanel();
        this.pluginListPanel.setLayout(new BoxLayout(this.pluginListPanel, BoxLayout.Y_AXIS));

        JPanel topPanel = new JPanel(new BorderLayout());
        JLabel noticeLabel = new JLabel(" GitHub Copilot for JetBrains is required to use installed custom agents and skills.");
        noticeLabel.setBorder(JBUI.Borders.empty(0, 4, 6, 4));
        topPanel.add(noticeLabel, BorderLayout.NORTH);

        JPanel toolbarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchField = new SearchTextField();
        searchField.getTextEditor().getDocument().addDocumentListener(new com.intellij.ui.DocumentAdapter() {
            @Override
            protected void textChanged(@NotNull javax.swing.event.DocumentEvent e) { updateList(); }
        });
        
        categoryFilter = new JComboBox<>(new String[]{"All Categories", "Business Workflows", "Engineering Workflows", "Java Engineering", "React Native Engineering", "Architecture Workflows", "Other"});
        categoryFilter.addActionListener(e -> updateList());
        
        installedFilter = new JCheckBox("Installed Only");
        installedFilter.addActionListener(e -> updateList());
        
        JButton refreshBtn = new JButton("Refresh Catalog", AllIcons.Actions.Refresh);
        refreshBtn.setToolTipText("Refresh Catalog");
        refreshBtn.addActionListener(e -> refreshCatalog());
        
        JButton settingsBtn = new JButton("Settings", AllIcons.General.Settings);
        settingsBtn.setToolTipText("Settings");
        settingsBtn.addActionListener(e -> ShowSettingsUtil.getInstance().showSettingsDialog(project, "com.digitalai.workbench.settings.WorkbenchSettingsConfigurable"));
        
        toolbarPanel.add(searchField);
        toolbarPanel.add(categoryFilter);
        toolbarPanel.add(installedFilter);
        toolbarPanel.add(refreshBtn);
        toolbarPanel.add(settingsBtn);
        
        topPanel.add(toolbarPanel, BorderLayout.CENTER);
        mainPanel.add(topPanel, BorderLayout.NORTH);
        
        emptyStateLabel = new JLabel("Loading catalog...", SwingConstants.CENTER);
        emptyStateLabel.setBorder(JBUI.Borders.empty(24));
        pluginListPanel.add(emptyStateLabel);

        JBScrollPane scrollPane = new JBScrollPane(pluginListPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBorder(JBUI.Borders.empty());
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Status bar
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusLabel = new JLabel(" Ready");
        statusPanel.add(statusLabel, BorderLayout.WEST);
        mainPanel.add(statusPanel, BorderLayout.SOUTH);
    }

    public JPanel getContent() {
        return mainPanel;
    }

    public void onToolWindowOpened() {
        WorkbenchSettings.State state = WorkbenchSettings.getInstance().getState();
        if (state != null && state.autoRefresh) {
            refreshCatalog();
        } else {
            loadCachedCatalog();
        }
    }

    private void setStatus(String text) {
        ApplicationManager.getApplication().invokeLater(() -> statusLabel.setText(" " + text));
    }

    private void loadCachedCatalog() {
        WorkbenchSettings.State state = WorkbenchSettings.getInstance().getState();
        if (state == null) return;
        
        File catalogDir = catalogRepo.getCatalogDirectory(state.marketplaceUrl, state.branch);
        if (catalogDir.exists()) {
            setStatus("Loading cached catalog... (Warning: Data may be stale)");
            parseAndShowCatalog(catalogDir);
        } else {
            setEmptyState("No catalog cache found. Fetching marketplace...");
            refreshCatalog();
        }
    }

    private void refreshCatalog() {
        WorkbenchSettings.State state = WorkbenchSettings.getInstance().getState();
        if (state == null) return;

        ProgressManager.getInstance().run(new Task.Backgroundable(project, "Refreshing Digital AI Workbench Catalog", false) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                setStatus("Fetching marketplace...");
                boolean success = catalogRepo.fetchOrClone(state.marketplaceUrl, state.branch, state.gitExecutable);
                if (success) {
                    File catalogDir = catalogRepo.getCatalogDirectory(state.marketplaceUrl, state.branch);
                    ApplicationManager.getApplication().invokeLater(() -> {
                        setStatus("Marketplace updated.");
                        parseAndShowCatalog(catalogDir);
                    });
                } else {
                    ApplicationManager.getApplication().invokeLater(() -> {
                        setStatus("Failed to fetch marketplace.");
                        File catalogDir = catalogRepo.getCatalogDirectory(state.marketplaceUrl, state.branch);
                        if (catalogDir.exists()) {
                            setEmptyState("Refresh failed. Showing the last cached catalog.");
                            parseAndShowCatalog(catalogDir);
                        } else {
                            setEmptyState("Unable to fetch marketplace. Open Settings to check the Git URL and credentials, then click Refresh Catalog.");
                            Messages.showErrorDialog(project, "Unable to fetch marketplace from " + state.marketplaceUrl, "Refresh Failed");
                        }
                    });
                }
            }
        });
    }

    private void parseAndShowCatalog(File catalogDir) {
        this.currentCatalogDir = catalogDir;
        this.currentManifest = parser.parseMarketplace(catalogDir);
        if (currentManifest == null || !validator.validateMarketplace(currentManifest)) {
            setStatus("Failed to parse or validate marketplace manifest.");
            setEmptyState("Invalid marketplace format. Check the configured repository and branch.");
            Messages.showErrorDialog(project, "Invalid marketplace format.", "Parse Error");
            return;
        }
        updateList();
    }

    private void updateList() {
        pluginListPanel.removeAll();
        if (currentManifest == null) {
            setEmptyState("No catalog loaded. Click Refresh Catalog.");
            pluginListPanel.revalidate();
            pluginListPanel.repaint();
            return;
        }

        String search = searchField.getText().toLowerCase();
        String catFilter = (String) categoryFilter.getSelectedItem();
        boolean installedOnly = installedFilter.isSelected();
        
        InstallationRegistry registry = registryService.getRegistry();

        List<MarketplacePluginEntry> plugins = currentManifest.getPlugins();
        if (plugins == null || plugins.isEmpty()) {
            setEmptyState("Marketplace contains no plugins.");
            pluginListPanel.revalidate();
            pluginListPanel.repaint();
            return;
        }

        int visibleCount = 0;
        for (MarketplacePluginEntry entry : plugins) {
            String inferredCat = validator.inferCategory(entry);
            entry.setCategory(inferredCat);
            
            boolean isInstalled = registry.getInstalledPlugins().containsKey(entry.getName());
            if (installedOnly && !isInstalled) continue;
            
            if (catFilter != null && !catFilter.equals("All Categories") && !catFilter.equals(inferredCat)) {
                continue;
            }
            
            if (!search.isEmpty()) {
                boolean matches = entry.getName().toLowerCase().contains(search) || 
                                  (entry.getDescription() != null && entry.getDescription().toLowerCase().contains(search));
                if (!matches) continue;
            }

            PluginCardPanel card = new PluginCardPanel(entry, isInstalled, registry, this);
            pluginListPanel.add(card);
            pluginListPanel.add(Box.createVerticalStrut(10));
            visibleCount++;
        }

        if (visibleCount == 0) {
            String category = catFilter == null ? "selected category" : catFilter;
            setEmptyState("No plugins match the current filters. Category: " + category + ".");
        } else {
            setStatus("Showing " + visibleCount + " of " + plugins.size() + " plugins.");
        }

        pluginListPanel.revalidate();
        pluginListPanel.repaint();
    }

    private void setEmptyState(String text) {
        pluginListPanel.removeAll();
        emptyStateLabel.setText(text);
        pluginListPanel.add(emptyStateLabel);
        setStatus(text);
        pluginListPanel.revalidate();
        pluginListPanel.repaint();
    }

    public void installPlugin(MarketplacePluginEntry entry, Runnable onComplete) {
        ProgressManager.getInstance().run(new Task.Backgroundable(project, "Installing " + entry.getName(), false) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                try {
                    String validationError = validator.getPluginEntryValidationError(entry, currentCatalogDir);
                    if (validationError != null) {
                        throw new Exception(validationError);
                    }
                    File sourceDir = validator.resolvePluginSourceDirectory(entry, currentCatalogDir);
                    PluginManifest pluginManifest = parser.parsePlugin(sourceDir);
                    if (!validator.validatePluginManifest(pluginManifest, entry)) {
                        throw new Exception("Invalid plugin.json in source directory.");
                    }
                    
                    if (pluginManifest.hasUnsupportedComponents()) {
                        ApplicationManager.getApplication().invokeAndWait(() -> {
                            int result = Messages.showYesNoDialog(project, 
                                "This plugin contains components not supported by the IntelliJ installer version 0.1.0.\nOnly agents and skills will be installed.", 
                                "Unsupported Components", Messages.getWarningIcon());
                            if (result != Messages.YES) {
                                throw new RuntimeException("User cancelled installation due to unsupported components.");
                            }
                        });
                    }

                    String summary = installer.install(entry, pluginManifest, sourceDir);
                    ApplicationManager.getApplication().invokeLater(() -> {
                        Messages.showInfoMessage(project, summary, "Installation Complete");
                        onComplete.run();
                        updateList();
                    });
                } catch (Exception e) {
                    LOG.warn("Installation failed", e);
                    ApplicationManager.getApplication().invokeLater(() -> {
                        Messages.showErrorDialog(project, "Installation failed: " + e.getMessage(), "Error");
                        onComplete.run();
                    });
                }
            }
        });
    }

    public void uninstallPlugin(String pluginName, Runnable onComplete) {
        ProgressManager.getInstance().run(new Task.Backgroundable(project, "Uninstalling " + pluginName, false) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                try {
                    String summary = installer.uninstall(pluginName);
                    ApplicationManager.getApplication().invokeLater(() -> {
                        Messages.showInfoMessage(project, summary, "Uninstall Complete");
                        onComplete.run();
                        updateList();
                    });
                } catch (Exception e) {
                    LOG.warn("Uninstall failed", e);
                    ApplicationManager.getApplication().invokeLater(() -> {
                        Messages.showErrorDialog(project, "Uninstall failed: " + e.getMessage(), "Error");
                        onComplete.run();
                    });
                }
            }
        });
    }
}
