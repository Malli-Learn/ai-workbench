package com.digitalai.workbench.catalog;

import com.digitalai.workbench.catalog.model.MarketplaceManifest;
import com.digitalai.workbench.catalog.model.PluginManifest;
import org.junit.Test;
import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import static org.junit.Assert.*;

public class MarketplaceParserTest {
    @Test
    public void testParseMarketplace() throws Exception {
        File tempDir = Files.createTempDirectory("test").toFile();
        File pluginDir = new File(tempDir, ".github/plugin");
        pluginDir.mkdirs();
        File jsonFile = new File(pluginDir, "marketplace.json");
        
        try (FileWriter w = new FileWriter(jsonFile)) {
            w.write("{ \"name\": \"test-marketplace\", \"plugins\": [ { \"name\": \"plugin1\", \"source\": \"plugins/plugin1\" } ] }");
        }
        
        MarketplaceParser parser = new MarketplaceParser();
        MarketplaceManifest manifest = parser.parseMarketplace(tempDir);
        
        assertNotNull(manifest);
        assertEquals("test-marketplace", manifest.getName());
        assertEquals(1, manifest.getPlugins().size());
        assertEquals("plugin1", manifest.getPlugins().get(0).getName());
    }

    @Test
    public void testParsePlugin() throws Exception {
        File pluginDir = Files.createTempDirectory("test-plugin").toFile();
        File jsonFile = new File(pluginDir, "plugin.json");
        
        try (FileWriter w = new FileWriter(jsonFile)) {
            w.write("{ \"name\": \"plugin1\", \"agents\": \"my-agents/\", \"skills\": [\"skill1/\", \"skill2/\"] }");
        }
        
        MarketplaceParser parser = new MarketplaceParser();
        PluginManifest manifest = parser.parsePlugin(pluginDir);
        
        assertNotNull(manifest);
        assertEquals("plugin1", manifest.getName());
        assertEquals(1, manifest.getAgents().size());
        assertEquals("my-agents/", manifest.getAgents().get(0));
        assertEquals(2, manifest.getSkills().size());
        assertEquals("skill1/", manifest.getSkills().get(0));
        assertEquals("skill2/", manifest.getSkills().get(1));
    }
}
