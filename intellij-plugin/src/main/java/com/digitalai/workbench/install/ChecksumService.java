package com.digitalai.workbench.install;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.Comparator;
import java.util.stream.Stream;

public class ChecksumService {

    public String calculateChecksum(File fileOrDir) {
        if (!fileOrDir.exists()) {
            return null;
        }
        if (fileOrDir.isFile()) {
            return calculateFileChecksum(fileOrDir.toPath());
        } else {
            return calculateDirectoryChecksum(fileOrDir);
        }
    }

    private String calculateFileChecksum(Path file) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(Files.readAllBytes(file));
            return bytesToHex(digest.digest());
        } catch (Exception e) {
            return null;
        }
    }

    private String calculateDirectoryChecksum(File dir) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            Path root = dir.toPath();
            try (Stream<Path> paths = Files.walk(dir.toPath())) {
                paths.filter(Files::isRegularFile)
                     .sorted(Comparator.comparing(path -> root.relativize(path).toString()))
                     .forEach(path -> {
                         try {
                             digest.update(root.relativize(path).toString().replace(File.separatorChar, '/').getBytes("UTF-8"));
                             digest.update((byte) 0);
                             digest.update(Files.readAllBytes(path));
                         } catch (Exception e) {
                             // Ignore
                         }
                     });
            }
            return bytesToHex(digest.digest());
        } catch (Exception e) {
            return null;
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
