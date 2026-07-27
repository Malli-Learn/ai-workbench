package com.digitalai.workbench.install;

import org.junit.Test;

import java.nio.file.Path;

import static org.junit.Assert.*;

public class PersonalCopilotPathsTest {
    @Test
    public void resolvesWindowsProfileCopilotDirectories() {
        PersonalCopilotPaths paths = new PersonalCopilotPaths(Path.of("C:/Users/dev"), "Windows 11");

        assertEquals(Path.of("C:/Users/dev/.copilot/agents"), paths.agentsDirectory());
        assertEquals(Path.of("C:/Users/dev/.copilot/skills"), paths.skillsDirectory());
        assertEquals("Windows", paths.operatingSystemLabel());
    }

    @Test
    public void resolvesRegistryPathsThroughUserHome() {
        PersonalCopilotPaths paths = new PersonalCopilotPaths(Path.of("/Users/dev"), "Mac OS X");

        assertEquals(Path.of("/Users/dev/.copilot/agents/test.agent.md"), paths.resolveRegistryPath("~/.copilot/agents/test.agent.md"));
        assertEquals(Path.of("/Users/dev/.copilot/skills/test-skill"), paths.resolveRegistryPath("~/.copilot/skills/test-skill"));
        assertEquals("macOS", paths.operatingSystemLabel());
    }
}
