package com.digitalai.workbench.catalog;

import com.digitalai.workbench.catalog.model.MarketplaceManifest;
import com.digitalai.workbench.catalog.model.MarketplacePluginEntry;
import com.digitalai.workbench.catalog.model.PluginManifest;

import java.io.File;
import java.nio.file.InvalidPathException;
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
        return getPluginEntryValidationError(entry, repositoryRoot) == null;
    }

    public String getPluginEntryValidationError(MarketplacePluginEntry entry, File repositoryRoot) {
        if (entry == null) {
            return "Marketplace plugin entry is missing.";
        }
        if (repositoryRoot == null) {
            return "Catalog directory is not loaded. Refresh the catalog and try again.";
        }
        if (!repositoryRoot.exists() || !repositoryRoot.isDirectory()) {
            return "Catalog directory does not exist: " + repositoryRoot.getAbsolutePath();
        }
        String source = entry.getSource();
        if (source == null || source.trim().isEmpty()) {
            return "Marketplace plugin '" + entry.getName() + "' has an empty source path.";
        }

        Path sourcePath;
        try {
            sourcePath = Path.of(source.replace('\\', '/')).normalize();
        } catch (InvalidPathException e) {
            return "Marketplace plugin '" + entry.getName() + "' has an invalid source path: " + source;
        }

        if (sourcePath.isAbsolute() || sourcePath.startsWith("..") || hasParentTraversal(sourcePath)) {
            return "Marketplace plugin '" + entry.getName() + "' source escapes the catalog: " + source;
        }

        File sourceDir = repositoryRoot.toPath().resolve(sourcePath).toFile();
        try {
            File repoCanonicalFile = repositoryRoot.getCanonicalFile();
            File sourceCanonicalFile = sourceDir.getCanonicalFile();
            Path repoCanonicalPath = repoCanonicalFile.toPath();
            Path sourceCanonicalPath = sourceCanonicalFile.toPath();

            if (!startsWith(sourceCanonicalPath, repoCanonicalPath)) {
                return "Marketplace plugin '" + entry.getName() + "' source escapes the catalog: " + sourceCanonicalFile.getAbsolutePath();
            }
            if (!sourceDir.exists() || !sourceDir.isDirectory()) {
                return "Marketplace plugin '" + entry.getName() + "' source directory does not exist: " + sourceDir.getAbsolutePath();
            }
            return null;
        } catch (Exception e) {
            return "Unable to validate marketplace plugin '" + entry.getName() + "' source path: " + e.getMessage();
        }
    }

    public File resolvePluginSourceDirectory(MarketplacePluginEntry entry, File repositoryRoot) {
        String source = entry.getSource().replace('\\', '/');
        return repositoryRoot.toPath().resolve(Path.of(source).normalize()).toFile();
    }

    private boolean hasParentTraversal(Path path) {
        for (Path segment : path) {
            if ("..".equals(segment.toString())) {
                return true;
            }
        }
        return false;
    }

    private boolean startsWith(Path child, Path root) {
        if (child.startsWith(root)) {
            return true;
        }
        if (File.separatorChar == '\\') {
            return child.toString().toLowerCase().startsWith(root.toString().toLowerCase());
        }
        return false;
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
