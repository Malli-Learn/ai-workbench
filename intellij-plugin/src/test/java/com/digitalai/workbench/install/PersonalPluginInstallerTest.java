package com.digitalai.workbench.install;

import com.digitalai.workbench.catalog.MarketplaceParser;
import com.digitalai.workbench.catalog.model.MarketplacePluginEntry;
import com.digitalai.workbench.catalog.model.PluginManifest;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;

public class PersonalPluginInstallerTest {
    @Test
    public void copiesAgentsAndRecursiveSkills() throws Exception {
        File root = Files.createTempDirectory("workbench-plugin").toFile();
        File home = Files.createTempDirectory("workbench-home").toFile();
        File pluginDir = createPlugin(root, "java-unit-test-coverage", "0.1.0", "agents/", "skills/");
        writeFile(new File(pluginDir, "agents/nested/java-unit-test-coverage.agent.md"), "agent");
        writeFile(new File(pluginDir, "skills/java-unit-test-generation/SKILL.md"), "skill");
        writeFile(new File(pluginDir, "skills/java-unit-test-generation/templates/template.md"), "template");
        writeFile(new File(pluginDir, "skills/java-unit-test-generation/scripts/run.sh"), "script");

        InstallationRegistry registry = new InstallationRegistry();
        PersonalPluginInstaller installer = installer(registry, home);

        installer.install(entry("java-unit-test-coverage", "0.1.0", "plugins/java"), manifest(pluginDir), pluginDir);

        assertTrue(new File(home, ".copilot/agents/java-unit-test-coverage.agent.md").isFile());
        assertTrue(new File(home, ".copilot/skills/java-unit-test-generation/SKILL.md").isFile());
        assertTrue(new File(home, ".copilot/skills/java-unit-test-generation/templates/template.md").isFile());
        assertTrue(new File(home, ".copilot/skills/java-unit-test-generation/scripts/run.sh").isFile());
        assertEquals("0.1.0", registry.getInstalledPlugins().get("java-unit-test-coverage").getVersion());
    }

    @Test
    public void rejectsUnmanagedAgentCollision() throws Exception {
        File root = Files.createTempDirectory("workbench-plugin").toFile();
        File home = Files.createTempDirectory("workbench-home").toFile();
        File pluginDir = createPlugin(root, "java-unit-test-coverage", "0.1.0", "agents/", "skills/");
        writeFile(new File(pluginDir, "agents/java-unit-test-coverage.agent.md"), "agent");
        writeFile(new File(home, ".copilot/agents/java-unit-test-coverage.agent.md"), "user-owned");

        PersonalPluginInstaller installer = installer(new InstallationRegistry(), home);

        Exception error = assertThrows(Exception.class, () ->
            installer.install(entry("java-unit-test-coverage", "0.1.0", "plugins/java"), manifest(pluginDir), pluginDir)
        );
        assertTrue(error.getMessage().contains("will not overwrite"));
    }

    @Test
    public void sharesIdenticalSkillUntilLastOwnerUninstalls() throws Exception {
        File root = Files.createTempDirectory("workbench-plugin").toFile();
        File home = Files.createTempDirectory("workbench-home").toFile();
        File first = createPlugin(root, "first-plugin", "0.1.0", "agents/", "skills/");
        File second = createPlugin(root, "second-plugin", "0.1.0", "agents/", "skills/");
        writeFile(new File(first, "skills/shared-skill/SKILL.md"), "same");
        writeFile(new File(second, "skills/shared-skill/SKILL.md"), "same");

        InstallationRegistry registry = new InstallationRegistry();
        PersonalPluginInstaller installer = installer(registry, home);

        installer.install(entry("first-plugin", "0.1.0", "plugins/first"), manifest(first), first);
        installer.install(entry("second-plugin", "0.1.0", "plugins/second"), manifest(second), second);

        assertTrue(new File(home, ".copilot/skills/shared-skill/SKILL.md").isFile());
        assertEquals(2, registry.getSharedResources().get("~/.copilot/skills/shared-skill").getOwners().size());

        installer.uninstall("first-plugin");
        assertTrue(new File(home, ".copilot/skills/shared-skill/SKILL.md").isFile());

        installer.uninstall("second-plugin");
        assertFalse(new File(home, ".copilot/skills/shared-skill").exists());
    }

    @Test
    public void rejectsDifferentSharedSkillContent() throws Exception {
        File root = Files.createTempDirectory("workbench-plugin").toFile();
        File home = Files.createTempDirectory("workbench-home").toFile();
        File first = createPlugin(root, "first-plugin", "0.1.0", "agents/", "skills/");
        File second = createPlugin(root, "second-plugin", "0.1.0", "agents/", "skills/");
        writeFile(new File(first, "skills/shared-skill/SKILL.md"), "same");
        writeFile(new File(second, "skills/shared-skill/SKILL.md"), "different");

        InstallationRegistry registry = new InstallationRegistry();
        PersonalPluginInstaller installer = installer(registry, home);
        installer.install(entry("first-plugin", "0.1.0", "plugins/first"), manifest(first), first);

        Exception error = assertThrows(Exception.class, () ->
            installer.install(entry("second-plugin", "0.1.0", "plugins/second"), manifest(second), second)
        );
        assertTrue(error.getMessage().contains("different content"));
    }

    @Test
    public void rejectsUpdateWhenManagedFileWasModified() throws Exception {
        File root = Files.createTempDirectory("workbench-plugin").toFile();
        File home = Files.createTempDirectory("workbench-home").toFile();
        File pluginDir = createPlugin(root, "java-unit-test-coverage", "0.1.0", "agents/", "skills/");
        writeFile(new File(pluginDir, "agents/java-unit-test-coverage.agent.md"), "agent");

        InstallationRegistry registry = new InstallationRegistry();
        PersonalPluginInstaller installer = installer(registry, home);
        installer.install(entry("java-unit-test-coverage", "0.1.0", "plugins/java"), manifest(pluginDir), pluginDir);

        writeFile(new File(home, ".copilot/agents/java-unit-test-coverage.agent.md"), "modified");

        Exception error = assertThrows(Exception.class, () ->
            installer.install(entry("java-unit-test-coverage", "0.2.0", "plugins/java"), manifest(pluginDir), pluginDir)
        );
        assertTrue(error.getMessage().contains("modified after installation"));
    }

    private PersonalPluginInstaller installer(InstallationRegistry registry, File home) {
        AtomicReference<InstallationRegistry> saved = new AtomicReference<>(registry);
        return new PersonalPluginInstaller(saved::get, saved::set, home);
    }

    private MarketplacePluginEntry entry(String name, String version, String source) {
        MarketplacePluginEntry entry = new MarketplacePluginEntry();
        entry.setName(name);
        entry.setVersion(version);
        entry.setSource(source);
        return entry;
    }

    private PluginManifest manifest(File pluginDir) {
        return new MarketplaceParser().parsePlugin(pluginDir);
    }

    private File createPlugin(File root, String name, String version, String agents, String skills) throws Exception {
        File pluginDir = new File(root, name);
        pluginDir.mkdirs();
        writeFile(new File(pluginDir, "plugin.json"), "{\n" +
            "  \"name\": \"" + name + "\",\n" +
            "  \"version\": \"" + version + "\",\n" +
            "  \"agents\": \"" + agents + "\",\n" +
            "  \"skills\": \"" + skills + "\"\n" +
            "}");
        return pluginDir;
    }

    private void writeFile(File file, String text) throws Exception {
        file.getParentFile().mkdirs();
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(text);
        }
    }
}
