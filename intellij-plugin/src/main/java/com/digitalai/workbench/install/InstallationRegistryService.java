package com.digitalai.workbench.install;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.diagnostic.Logger;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class InstallationRegistryService {
    private static final Logger LOG = Logger.getInstance(InstallationRegistryService.class);
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private InstallationRegistry currentRegistry;

    public static InstallationRegistryService getInstance() {
        return ApplicationManager.getApplication().getService(InstallationRegistryService.class);
    }

    public synchronized InstallationRegistry getRegistry() {
        if (currentRegistry == null) {
            currentRegistry = loadRegistry();
        }
        return currentRegistry;
    }

    public synchronized void saveRegistry(InstallationRegistry registry) {
        this.currentRegistry = registry;
        File registryFile = getRegistryFile();
        File tempFile = new File(registryFile.getParentFile(), registryFile.getName() + ".tmp");
        File backupFile = new File(registryFile.getParentFile(), registryFile.getName() + ".bak");

        try {
            if (!registryFile.getParentFile().exists()) {
                registryFile.getParentFile().mkdirs();
            }

            try (FileWriter writer = new FileWriter(tempFile)) {
                gson.toJson(registry, writer);
                writer.flush();
            }

            if (registryFile.exists()) {
                Files.copy(registryFile.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }

            Files.move(tempFile.toPath(), registryFile.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            LOG.error("Failed to save installation registry", e);
        }
    }

    private InstallationRegistry loadRegistry() {
        File registryFile = getRegistryFile();
        if (registryFile.exists()) {
            try (FileReader reader = new FileReader(registryFile)) {
                InstallationRegistry reg = gson.fromJson(reader, InstallationRegistry.class);
                if (reg != null) return reg;
            } catch (Exception e) {
                LOG.warn("Failed to read registry, attempting recovery from backup", e);
                return recoverFromBackup();
            }
        }
        return new InstallationRegistry();
    }

    private InstallationRegistry recoverFromBackup() {
        File backupFile = new File(getRegistryFile().getParentFile(), getRegistryFile().getName() + ".bak");
        if (backupFile.exists()) {
            try (FileReader reader = new FileReader(backupFile)) {
                InstallationRegistry reg = gson.fromJson(reader, InstallationRegistry.class);
                if (reg != null) return reg;
            } catch (Exception e) {
                LOG.error("Failed to recover registry from backup", e);
            }
        }
        return new InstallationRegistry();
    }

    private File getRegistryFile() {
        return new File(PathManager.getConfigPath(), "digital-ai-workbench/installed.json");
    }
}
