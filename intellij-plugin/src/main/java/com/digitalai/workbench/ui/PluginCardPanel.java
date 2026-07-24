package com.digitalai.workbench.ui;

import com.digitalai.workbench.catalog.model.MarketplacePluginEntry;
import com.digitalai.workbench.install.InstallationRegistry;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import java.awt.*;

public class PluginCardPanel extends JPanel {
    private final JButton actionButton;
    private final JButton uninstallButton;

    public PluginCardPanel(MarketplacePluginEntry entry, boolean isInstalled, InstallationRegistry registry, WorkbenchPanel parent) {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(JBUI.CurrentTheme.CustomFrameDecorations.separatorForeground(), 1, true),
            JBUI.Borders.empty(10)
        ));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);

        JBLabel nameLabel = new JBLabel("<html><b>" + entry.getName() + "</b></html>");
        infoPanel.add(nameLabel);
        
        JBLabel descLabel = new JBLabel("<html>" + (entry.getDescription() != null ? entry.getDescription() : "") + "</html>");
        descLabel.setForeground(JBUI.CurrentTheme.Label.disabledForeground());
        infoPanel.add(descLabel);
        
        JBLabel metaLabel = new JBLabel(String.format("<html>Category: %s<br>Available: %s", 
            entry.getCategory() != null ? entry.getCategory() : "Other", 
            entry.getVersion() != null ? entry.getVersion() : "Unknown"
        ));
        
        String installedVersion = null;
        if (isInstalled) {
            InstallationRegistry.InstalledPluginInfo info = registry.getInstalledPlugins().get(entry.getName());
            if (info != null) {
                installedVersion = info.getVersion();
                metaLabel.setText(metaLabel.getText() + "<br>Installed: " + installedVersion);
            }
        } else {
            metaLabel.setText(metaLabel.getText() + "<br>Installed: Not installed");
        }
        
        metaLabel.setText(metaLabel.getText() + "</html>");
        infoPanel.add(Box.createVerticalStrut(5));
        infoPanel.add(metaLabel);

        add(infoPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false);
        
        actionButton = new JButton();
        uninstallButton = new JButton("Uninstall");
        uninstallButton.setVisible(false);

        if (isInstalled) {
            if (installedVersion != null && entry.getVersion() != null && !installedVersion.equals(entry.getVersion())) {
                actionButton.setText("Update");
                actionButton.addActionListener(e -> {
                    setButtonsEnabled(false);
                    parent.installPlugin(entry, () -> setButtonsEnabled(true));
                });
            } else {
                actionButton.setText("Reinstall");
                actionButton.addActionListener(e -> {
                    setButtonsEnabled(false);
                    parent.installPlugin(entry, () -> setButtonsEnabled(true));
                });
            }
            uninstallButton.setVisible(true);
            uninstallButton.addActionListener(e -> {
                setButtonsEnabled(false);
                parent.uninstallPlugin(entry.getName(), () -> setButtonsEnabled(true));
            });
        } else {
            actionButton.setText("Install for Me");
            actionButton.addActionListener(e -> {
                setButtonsEnabled(false);
                parent.installPlugin(entry, () -> setButtonsEnabled(true));
            });
        }

        buttonPanel.add(actionButton);
        buttonPanel.add(uninstallButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void setButtonsEnabled(boolean enabled) {
        actionButton.setEnabled(enabled);
        uninstallButton.setEnabled(enabled);
    }
}
