package com.digitalai.workbench.install;

import java.io.File;
import java.nio.file.Path;
import java.util.Locale;

public class PersonalCopilotPaths {
    static final String AGENTS_REGISTRY_PREFIX = "~/.copilot/agents/";
    static final String SKILLS_REGISTRY_PREFIX = "~/.copilot/skills/";

    private final Path userHome;
    private final String operatingSystem;

    public PersonalCopilotPaths(File userHome) {
        this(userHome.toPath(), System.getProperty("os.name", "unknown"));
    }

    public PersonalCopilotPaths(Path userHome, String operatingSystem) {
        this.userHome = userHome;
        this.operatingSystem = operatingSystem == null ? "unknown" : operatingSystem;
    }

    public Path agentsDirectory() {
        return userHome.resolve(".copilot").resolve("agents");
    }

    public Path skillsDirectory() {
        return userHome.resolve(".copilot").resolve("skills");
    }

    public String agentRegistryPath(String filename) {
        return AGENTS_REGISTRY_PREFIX + filename;
    }

    public String skillRegistryPath(String directoryName) {
        return SKILLS_REGISTRY_PREFIX + directoryName;
    }

    public Path resolveRegistryPath(String registryPath) {
        if (registryPath.startsWith(AGENTS_REGISTRY_PREFIX)) {
            return agentsDirectory().resolve(registryPath.substring(AGENTS_REGISTRY_PREFIX.length()));
        }
        if (registryPath.startsWith(SKILLS_REGISTRY_PREFIX)) {
            return skillsDirectory().resolve(registryPath.substring(SKILLS_REGISTRY_PREFIX.length()));
        }
        if (registryPath.startsWith("~/")) {
            return userHome.resolve(registryPath.substring(2));
        }
        return Path.of(registryPath);
    }

    public String operatingSystemLabel() {
        String lowerName = operatingSystem.toLowerCase(Locale.ROOT);
        if (lowerName.contains("win")) return "Windows";
        if (lowerName.contains("mac")) return "macOS";
        if (lowerName.contains("linux")) return "Linux";
        return operatingSystem;
    }
}
