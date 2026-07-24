package com.digitalai.workbench.catalog;

import com.digitalai.workbench.catalog.model.MarketplaceManifest;
import com.digitalai.workbench.catalog.model.MarketplacePluginEntry;
import com.digitalai.workbench.catalog.model.PluginManifest;

import java.io.File;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

public class MarketplaceValidator {
    private static final Pattern KEBAB_CASE = Pattern.compile("^[a-z0-9]+(-[a-z0-9]+)*$");
    
    public boolean validateMarketplace(MarketplaceManifest manifest) {
        if (manifest == null || manifest.getName() == null || manifest.getName().trim().isEmpty()) {
            return false;
        }
        Set<String> names = new HashSet<>();
        if (manifest.getPlugins() != null) {
            for (MarketplacePluginEntry entry : manifest.getPlugins()) {
                if (entry.getName() == null || !KEBAB_CASE.matcher(entry.getName()).matches()) {
                    return false;
                }
                if (!names.add(entry.getName())) {
                    return false; // Duplicate name
                }
                if (entry.getSource() == null || entry.getSource().trim().isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean validatePluginEntry(MarketplacePluginEntry entry, File repositoryRoot) {
        if (entry == null || entry.getSource() == null || entry.getSource().contains("..")) {
            return false;
        }
        File sourceDir = new File(repositoryRoot, entry.getSource());
        try {
            Path repoCanonicalPath = repositoryRoot.getCanonicalFile().toPath();
            Path sourceCanonicalPath = sourceDir.getCanonicalFile().toPath();
            
            if (!sourceCanonicalPath.startsWith(repoCanonicalPath)) {
                return false; // Path traversal or symlink escape
            }
            if (!sourceDir.exists() || !sourceDir.isDirectory()) {
                return false;
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean validatePluginManifest(PluginManifest manifest, MarketplacePluginEntry entry) {
        if (manifest == null) return false;
        return entry.getName().equals(manifest.getName());
    }

    public String inferCategory(MarketplacePluginEntry entry) {
        if (entry.getCategory() != null && !entry.getCategory().trim().isEmpty()) {
            return entry.getCategory();
        }
        
        String source = entry.getSource();
        if (source != null) {
            if (source.contains("business-workflows")) return "Business Workflows";
            if (source.contains("engineering-workflows")) return "Engineering Workflows";
            if (source.contains("java-engineering")) return "Java Engineering";
            if (source.contains("react-native-engineering")) return "React Native Engineering";
            if (source.contains("architecture-workflows")) return "Architecture Workflows";
        }
        return "Other";
    }
}
