package com.digitalai.workbench.install;

import com.digitalai.workbench.catalog.model.MarketplacePluginEntry;
import com.digitalai.workbench.catalog.model.PluginManifest;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class PersonalPluginInstaller {
    private final ChecksumService checksumService = new ChecksumService();
    private final Supplier<InstallationRegistry> registrySupplier;
    private final Consumer<InstallationRegistry> registrySaver;
    private final PersonalCopilotPaths copilotPaths;

    public PersonalPluginInstaller() {
        InstallationRegistryService registryService = InstallationRegistryService.getInstance();
        this.registrySupplier = registryService::getRegistry;
        this.registrySaver = registryService::saveRegistry;
        this.copilotPaths = new PersonalCopilotPaths(new File(System.getProperty("user.home")));
    }

    public PersonalPluginInstaller(Supplier<InstallationRegistry> registrySupplier, Consumer<InstallationRegistry> registrySaver, File userHome) {
        this.registrySupplier = registrySupplier;
        this.registrySaver = registrySaver;
        this.copilotPaths = new PersonalCopilotPaths(userHome);
    }

    public String install(MarketplacePluginEntry entry, PluginManifest manifest, File pluginSourceDir) throws Exception {
        InstallationRegistry registry = registrySupplier.get();
        Path agentsTargetDir = copilotPaths.agentsDirectory();
        Path skillsTargetDir = copilotPaths.skillsDirectory();

        verifyExistingInstallation(entry.getName(), registry);

        InstallationRegistry.InstalledPluginInfo installedInfo = new InstallationRegistry.InstalledPluginInfo();
        installedInfo.setVersion(entry.getVersion());
        installedInfo.setInstalledAt(Instant.now().toString());
        installedInfo.setAgents(new ArrayList<>());
        installedInfo.setSkills(new ArrayList<>());

        List<File> tempFiles = new ArrayList<>();

        try {
            List<File> agentFiles = discoverAgentFiles(pluginSourceDir, manifest);
            List<File> skillDirs = discoverSkillDirectories(pluginSourceDir, manifest);

            for (File agentFile : agentFiles) {
                Path targetFile = agentsTargetDir.resolve(agentFile.getName());
                String sourceChecksum = checksumService.calculateChecksum(agentFile);
                checkConflict(targetFile.toFile(), sourceChecksum, registry, false);

                Files.createDirectories(agentsTargetDir);
                File tempFile = Files.createTempFile(agentsTargetDir.getParent(), "agent-", ".md").toFile();
                Files.copy(agentFile.toPath(), tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                tempFiles.add(tempFile);

                InstallationRegistry.ComponentInfo info = componentInfo(agentFile.getName(), copilotPaths.agentRegistryPath(agentFile.getName()), sourceChecksum);
                installedInfo.getAgents().add(info);

                Files.move(tempFile.toPath(), targetFile, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            }

            for (File skillDir : skillDirs) {
                Path targetDir = skillsTargetDir.resolve(skillDir.getName());
                String sourceChecksum = checksumService.calculateChecksum(skillDir);
                checkConflict(targetDir.toFile(), sourceChecksum, registry, true);

                InstallationRegistry.ComponentInfo info = componentInfo(skillDir.getName(), copilotPaths.skillRegistryPath(skillDir.getName()), sourceChecksum);
                installedInfo.getSkills().add(info);

                if (!Files.exists(targetDir)) {
                    Files.createDirectories(skillsTargetDir);
                    File tempDir = Files.createTempDirectory(skillsTargetDir, "skill-").toFile();
                    copyDirectory(skillDir.toPath(), tempDir.toPath());
                    tempFiles.add(tempDir);
                    Files.move(tempDir.toPath(), targetDir, StandardCopyOption.ATOMIC_MOVE);
                }
                addSharedOwner(registry, info.getPath(), sourceChecksum, entry.getName());
            }

            registry.getInstalledPlugins().put(entry.getName(), installedInfo);
            registrySaver.accept(registry);

            return String.format("%s installed successfully.\n\nAgents installed: %d\nSkills installed: %d\n\nDetected OS: %s\nPersonal agent path:\n%s\nPersonal skill path:\n%s\n\nRestart IntelliJ or reopen GitHub Copilot Chat to reload personal customizations.",
                    entry.getName(),
                    installedInfo.getAgents().size(),
                    installedInfo.getSkills().size(),
                    copilotPaths.operatingSystemLabel(),
                    copilotPaths.agentsDirectory(),
                    copilotPaths.skillsDirectory());
        } catch (Exception e) {
            for (File temp : tempFiles) {
                deleteDirectory(temp);
            }
            throw e;
        }
    }

    public String uninstall(String pluginName) throws Exception {
        InstallationRegistry registry = registrySupplier.get();
        InstallationRegistry.InstalledPluginInfo info = registry.getInstalledPlugins().get(pluginName);
        if (info == null) {
            throw new Exception("Plugin not installed.");
        }

        List<String> retainedFiles = new ArrayList<>();

        for (InstallationRegistry.ComponentInfo agent : info.getAgents()) {
            File targetFile = resolvePath(agent.getPath());
            if (targetFile.exists()) {
                String currentChecksum = checksumService.calculateChecksum(targetFile);
                if (currentChecksum != null && !currentChecksum.equals(agent.getChecksum())) {
                    retainedFiles.add(agent.getPath());
                } else {
                    targetFile.delete();
                }
            }
        }

        for (InstallationRegistry.ComponentInfo skill : info.getSkills()) {
            File targetDir = resolvePath(skill.getPath());
            InstallationRegistry.SharedResourceInfo shared = registry.getSharedResources().get(skill.getPath());
            if (shared != null) {
                shared.getOwners().remove(pluginName);
                if (!shared.getOwners().isEmpty()) {
                    continue;
                }
                registry.getSharedResources().remove(skill.getPath());
            }
            if (targetDir.exists()) {
                String currentChecksum = checksumService.calculateChecksum(targetDir);
                if (currentChecksum != null && !currentChecksum.equals(skill.getChecksum())) {
                    retainedFiles.add(skill.getPath());
                } else {
                    deleteDirectory(targetDir);
                }
            }
        }

        registry.getInstalledPlugins().remove(pluginName);
        registrySaver.accept(registry);

        StringBuilder summary = new StringBuilder();
        if (retainedFiles.isEmpty()) {
            summary.append("Plugin uninstalled successfully.");
        } else {
            summary.append("Plugin uninstalled, but modified files were retained:\n");
            for (String file : retainedFiles) {
                summary.append(file).append("\n");
            }
        }
        return summary.toString();
    }

    private void verifyExistingInstallation(String pluginName, InstallationRegistry registry) throws Exception {
        InstallationRegistry.InstalledPluginInfo info = registry.getInstalledPlugins().get(pluginName);
        if (info == null) return;
        for (InstallationRegistry.ComponentInfo agent : info.getAgents()) {
            verifyManagedComponentUnmodified(agent, pluginName);
        }
        for (InstallationRegistry.ComponentInfo skill : info.getSkills()) {
            verifyManagedComponentUnmodified(skill, pluginName);
        }
    }

    private void verifyManagedComponentUnmodified(InstallationRegistry.ComponentInfo component, String pluginName) throws Exception {
        File target = resolvePath(component.getPath());
        if (!target.exists()) return;
        String currentChecksum = checksumService.calculateChecksum(target);
        if (currentChecksum != null && !currentChecksum.equals(component.getChecksum())) {
            throw new Exception("The installed file was modified after installation: " + component.getPath() + "\nUpdate of " + pluginName + " cannot continue until the conflict is resolved.");
        }
    }

    private void checkConflict(File target, String sourceChecksum, InstallationRegistry registry, boolean shareable) throws Exception {
        if (!target.exists()) return;

        String existingChecksum = checksumService.calculateChecksum(target);
        String targetPath = shareable ? copilotPaths.skillRegistryPath(target.getName()) : copilotPaths.agentRegistryPath(target.getName());
        InstallationRegistry.ComponentInfo managedComponent = findManagedComponent(registry, targetPath);

        if (managedComponent == null) {
            throw new Exception("A personal customization already exists:\n" + target.getAbsolutePath() + "\nDigital AI Workbench will not overwrite files it does not manage.");
        }

        if (existingChecksum != null && !existingChecksum.equals(managedComponent.getChecksum())) {
            throw new Exception("The installed file was modified after installation: " + targetPath + "\nUpdate cannot continue until the conflict is resolved.");
        }

        if (sourceChecksum != null && !sourceChecksum.equals(managedComponent.getChecksum())) {
            throw new Exception("A managed " + (shareable ? "skill" : "agent") + " with the same name already exists but has different content:\n" + target.getAbsolutePath());
        }
    }

    private InstallationRegistry.ComponentInfo findManagedComponent(InstallationRegistry registry, String path) {
        for (InstallationRegistry.InstalledPluginInfo info : registry.getInstalledPlugins().values()) {
            for (InstallationRegistry.ComponentInfo agent : safeComponents(info.getAgents())) {
                if (path.equals(agent.getPath())) return agent;
            }
            for (InstallationRegistry.ComponentInfo skill : safeComponents(info.getSkills())) {
                if (path.equals(skill.getPath())) return skill;
            }
        }
        return null;
    }

    private List<InstallationRegistry.ComponentInfo> safeComponents(List<InstallationRegistry.ComponentInfo> components) {
        return components == null ? List.of() : components;
    }

    private void addSharedOwner(InstallationRegistry registry, String path, String checksum, String pluginName) {
        InstallationRegistry.SharedResourceInfo shared = registry.getSharedResources().get(path);
        if (shared == null) {
            shared = new InstallationRegistry.SharedResourceInfo();
            shared.setResource(path);
            shared.setChecksum(checksum);
            shared.setOwners(new ArrayList<>());
            registry.getSharedResources().put(path, shared);
        }
        if (!shared.getOwners().contains(pluginName)) {
            shared.getOwners().add(pluginName);
        }
    }

    private InstallationRegistry.ComponentInfo componentInfo(String name, String path, String checksum) {
        InstallationRegistry.ComponentInfo info = new InstallationRegistry.ComponentInfo();
        info.setName(name);
        info.setPath(path);
        info.setChecksum(checksum);
        return info;
    }

    private List<File> discoverAgentFiles(File pluginSourceDir, PluginManifest manifest) throws Exception {
        List<File> agentFiles = new ArrayList<>();
        Set<String> names = new HashSet<>();
        for (String agentPath : manifest.getAgents()) {
            File sourceAgentDir = resolveComponentRoot(pluginSourceDir, agentPath);
            if (!sourceAgentDir.exists()) continue;
            try (Stream<Path> paths = Files.walk(sourceAgentDir.toPath())) {
                List<Path> matches = paths.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(".agent.md"))
                    .sorted(Comparator.comparing(Path::toString))
                    .toList();
                for (Path path : matches) {
                    File file = path.toFile();
                    if (!names.add(file.getName())) {
                        throw new Exception("Duplicate agent filename discovered: " + file.getName());
                    }
                    ensureInside(pluginSourceDir, file);
                    agentFiles.add(file);
                }
            }
        }
        return agentFiles;
    }

    private List<File> discoverSkillDirectories(File pluginSourceDir, PluginManifest manifest) throws Exception {
        List<File> skillDirs = new ArrayList<>();
        Set<String> names = new HashSet<>();
        for (String skillPath : manifest.getSkills()) {
            File sourceSkillRoot = resolveComponentRoot(pluginSourceDir, skillPath);
            if (!sourceSkillRoot.exists()) continue;
            try (Stream<Path> paths = Files.walk(sourceSkillRoot.toPath())) {
                List<Path> manifests = paths.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().equals("SKILL.md"))
                    .sorted(Comparator.comparing(Path::toString))
                    .toList();
                for (Path skillManifest : manifests) {
                    File skillDir = skillManifest.getParent().toFile();
                    if (!names.add(skillDir.getName())) {
                        throw new Exception("Duplicate skill directory discovered: " + skillDir.getName());
                    }
                    ensureInside(pluginSourceDir, skillDir);
                    skillDirs.add(skillDir);
                }
            }
        }
        return skillDirs;
    }

    private File resolveComponentRoot(File pluginSourceDir, String componentPath) throws Exception {
        if (componentPath == null || componentPath.trim().isEmpty() || componentPath.contains("..")) {
            throw new Exception("Invalid component path: " + componentPath);
        }
        File componentRoot = new File(pluginSourceDir, componentPath);
        ensureInside(pluginSourceDir, componentRoot);
        return componentRoot;
    }

    private void ensureInside(File root, File child) throws Exception {
        Path rootPath = root.getCanonicalFile().toPath();
        Path childPath = child.getCanonicalFile().toPath();
        if (!childPath.startsWith(rootPath)) {
            throw new Exception("Path escapes the plugin directory: " + child);
        }
    }

    private File resolvePath(String path) {
        return copilotPaths.resolveRegistryPath(path).toFile();
    }

    private void copyDirectory(Path source, Path target) throws IOException {
        Files.walkFileTree(source, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                Files.createDirectories(target.resolve(source.relativize(dir).toString()));
                return FileVisitResult.CONTINUE;
            }
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (Files.isSymbolicLink(file)) {
                    throw new IOException("Refusing to copy symlink: " + file);
                }
                Files.copy(file, target.resolve(source.relativize(file).toString()), StandardCopyOption.REPLACE_EXISTING);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private void deleteDirectory(File directoryToBeDeleted) {
        if (!directoryToBeDeleted.exists()) return;
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        directoryToBeDeleted.delete();
    }
}
