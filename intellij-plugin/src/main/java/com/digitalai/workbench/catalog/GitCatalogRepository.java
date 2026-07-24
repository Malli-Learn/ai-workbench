package com.digitalai.workbench.catalog;

import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.diagnostic.Logger;
import com.digitalai.workbench.util.GitCommandExecutor;
import com.digitalai.workbench.util.DefaultGitCommandExecutor;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class GitCatalogRepository {
    private static final Logger LOG = Logger.getInstance(GitCatalogRepository.class);
    private final GitCommandExecutor executor;

    public GitCatalogRepository() {
        this(new DefaultGitCommandExecutor());
    }

    public GitCatalogRepository(GitCommandExecutor executor) {
        this.executor = executor;
    }

    private String getGitPath(String configuredPath) {
        return (configuredPath == null || configuredPath.trim().isEmpty()) ? "git" : configuredPath.trim();
    }

    public boolean testConnection(String url, String branch, String gitExecutable) {
        if (containsCredentials(url)) {
            LOG.warn("Refusing to use repository URL containing credentials");
            return false;
        }
        return executor.execute(null, getGitPath(gitExecutable), "ls-remote", url, branch);
    }

    public File getCatalogDirectory(String url, String branch) {
        String hash = generateHash(url + "@" + branch);
        return new File(PathManager.getSystemPath(), "digital-ai-workbench/catalog/" + hash);
    }

    public boolean fetchOrClone(String url, String branch, String gitExecutable) {
        if (containsCredentials(url)) {
            LOG.warn("Refusing to use repository URL containing credentials");
            return false;
        }

        File targetDir = getCatalogDirectory(url, branch);
        String gitPath = getGitPath(gitExecutable);

        if (!targetDir.exists()) {
            LOG.info("Cloning marketplace from " + sanitizeUrl(url));
            targetDir.getParentFile().mkdirs();
            return executor.execute(targetDir.getParentFile(), 
                gitPath, "clone", "--depth", "1", "--branch", branch, url, targetDir.getName());
        } else {
            LOG.info("Refreshing marketplace from " + sanitizeUrl(url));
            boolean fetchSuccess = executor.execute(targetDir, gitPath, "fetch", "--depth", "1", "origin", branch);
            if (fetchSuccess) {
                return executor.execute(targetDir, gitPath, "reset", "--hard", "FETCH_HEAD");
            }
            return false;
        }
    }

    public void clearCache() {
        File cacheDir = new File(PathManager.getSystemPath(), "digital-ai-workbench/catalog");
        if (cacheDir.exists()) {
            deleteDirectory(cacheDir);
        }
    }

    private void deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        directoryToBeDeleted.delete();
    }

    private boolean containsCredentials(String url) {
        return url != null && url.matches("^[a-zA-Z][a-zA-Z0-9+.-]*://[^/@]+@.*");
    }

    private String sanitizeUrl(String url) {
        return url.replaceAll("://.*@(.*)", "://$1");
    }

    private String generateHash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder(2 * hash.length);
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            return String.valueOf(input.hashCode());
        }
    }
}
