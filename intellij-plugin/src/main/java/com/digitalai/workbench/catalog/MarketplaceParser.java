package com.digitalai.workbench.catalog;

import com.google.gson.Gson;
import com.digitalai.workbench.catalog.model.MarketplaceManifest;
import com.digitalai.workbench.catalog.model.PluginManifest;
import com.intellij.openapi.diagnostic.Logger;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;

public class MarketplaceParser {
    private static final Logger LOG = Logger.getInstance(MarketplaceParser.class);
    private final Gson gson = new Gson();

    private static final String[] FALLBACK_LOCATIONS = {
        ".github/plugin/marketplace.json",
        "marketplace.json",
        ".plugin/marketplace.json",
        ".claude-plugin/marketplace.json"
    };

    public MarketplaceManifest parseMarketplace(File repositoryRoot) {
        for (String location : FALLBACK_LOCATIONS) {
            File file = new File(repositoryRoot, location);
            if (file.exists() && file.isFile()) {
                try (Reader reader = new FileReader(file)) {
                    MarketplaceManifest manifest = gson.fromJson(reader, MarketplaceManifest.class);
                    if (manifest != null && manifest.getName() != null) {
                        return manifest;
                    }
                } catch (Exception e) {
                    LOG.warn("Failed to parse " + location, e);
                }
            }
        }
        return null;
    }

    public PluginManifest parsePlugin(File pluginDir) {
        File file = new File(pluginDir, "plugin.json");
        if (file.exists() && file.isFile()) {
            try (Reader reader = new FileReader(file)) {
                return gson.fromJson(reader, PluginManifest.class);
            } catch (Exception e) {
                LOG.warn("Failed to parse plugin.json in " + pluginDir, e);
            }
        }
        return null;
    }
}
