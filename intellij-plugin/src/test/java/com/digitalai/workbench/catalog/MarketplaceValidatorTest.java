package com.digitalai.workbench.catalog;

import com.digitalai.workbench.catalog.model.MarketplaceManifest;
import com.digitalai.workbench.catalog.model.MarketplacePluginEntry;
import com.digitalai.workbench.catalog.model.PluginManifest;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.util.List;

import static org.junit.Assert.*;

public class MarketplaceValidatorTest {
    @Test
    public void rejectsDuplicatePluginNames() {
        MarketplaceManifest manifest = new MarketplaceManifest();
        manifest.setName("test");
        manifest.setPlugins(List.of(entry("duplicate", "plugins/one"), entry("duplicate", "plugins/two")));

        assertFalse(new MarketplaceValidator().validateMarketplace(manifest));
    }

    @Test
    public void rejectsTraversalSourcePath() throws Exception {
        File root = Files.createTempDirectory("marketplace").toFile();
        MarketplacePluginEntry entry = entry("plugin-one", "../outside");

        assertFalse(new MarketplaceValidator().validatePluginEntry(entry, root));
    }

    @Test
    public void rejectsPluginNameMismatch() throws Exception {
        File pluginDir = Files.createTempDirectory("plugin").toFile();
        writeFile(new File(pluginDir, "plugin.json"), "{ \"name\": \"actual-name\" }");
        PluginManifest manifest = new MarketplaceParser().parsePlugin(pluginDir);

        assertFalse(new MarketplaceValidator().validatePluginManifest(manifest, entry("expected-name", "plugins/plugin")));
    }

    @Test
    public void infersKnownCategoryFromSourcePath() {
        MarketplacePluginEntry entry = entry("java-plugin", "plugins/java-engineering/java-plugin");

        assertEquals("Java Engineering", new MarketplaceValidator().inferCategory(entry));
    }

    private MarketplacePluginEntry entry(String name, String source) {
        MarketplacePluginEntry entry = new MarketplacePluginEntry();
        entry.setName(name);
        entry.setSource(source);
        return entry;
    }

    private void writeFile(File file, String text) throws Exception {
        file.getParentFile().mkdirs();
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(text);
        }
    }
}
