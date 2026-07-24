package com.digitalai.workbench.catalog;

import com.digitalai.workbench.util.GitCommandExecutor;
import org.junit.Test;
import java.io.File;
import static org.junit.Assert.*;

public class GitCatalogRepositoryTest {
    @Test
    public void testFetchOrCloneWithMock() {
        boolean[] executed = {false};
        GitCommandExecutor mockExecutor = new GitCommandExecutor() {
            @Override
            public boolean execute(File directory, String... command) {
                executed[0] = true;
                return true;
            }
        };
        
        GitCatalogRepository repo = new GitCatalogRepository(mockExecutor);
        boolean result = repo.fetchOrClone("https://github.com/mock/repo.git", "main", "git");
        assertTrue(result);
        assertTrue(executed[0]);
    }

    @Test
    public void testCloneCommandConstruction() {
        String[][] captured = new String[1][];
        GitCommandExecutor mockExecutor = new GitCommandExecutor() {
            @Override
            public boolean execute(File directory, String... command) {
                captured[0] = command;
                return true;
            }
        };

        GitCatalogRepository repo = new GitCatalogRepository(mockExecutor);
        assertTrue(repo.fetchOrClone("https://github.com/mock/repo.git", "main", "git"));

        assertArrayEquals(new String[]{"git", "clone", "--depth", "1", "--branch", "main", "https://github.com/mock/repo.git", captured[0][7]}, captured[0]);
    }

    @Test
    public void testConnectionCommandConstruction() {
        String[][] captured = new String[1][];
        GitCommandExecutor mockExecutor = new GitCommandExecutor() {
            @Override
            public boolean execute(File directory, String... command) {
                captured[0] = command;
                return true;
            }
        };

        GitCatalogRepository repo = new GitCatalogRepository(mockExecutor);
        assertTrue(repo.testConnection("https://github.com/mock/repo.git", "main", ""));

        assertArrayEquals(new String[]{"git", "ls-remote", "https://github.com/mock/repo.git", "main"}, captured[0]);
    }

    @Test
    public void testRefuseCredentials() {
        GitCommandExecutor mockExecutor = new GitCommandExecutor() {
            @Override
            public boolean execute(File directory, String... command) {
                return true;
            }
        };
        
        GitCatalogRepository repo = new GitCatalogRepository(mockExecutor);
        boolean result = repo.fetchOrClone("https://some-user:secret-token@github.com/mock/repo.git", "main", "git");
        assertFalse(result); // Should refuse embedded credentials
    }
}
