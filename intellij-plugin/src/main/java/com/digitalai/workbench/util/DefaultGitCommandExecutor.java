package com.digitalai.workbench.util;

import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.CapturingProcessHandler;
import com.intellij.execution.process.ProcessOutput;
import com.intellij.openapi.diagnostic.Logger;
import java.io.File;
import java.util.Arrays;
import java.util.Map;

public class DefaultGitCommandExecutor implements GitCommandExecutor {
    private static final Logger LOG = Logger.getInstance(DefaultGitCommandExecutor.class);
    private static final int TIMEOUT_MS = 60000;

    @Override
    public boolean execute(File directory, String... command) {
        try {
            GeneralCommandLine cmd = new GeneralCommandLine(command);
            if (directory != null) {
                cmd.setWorkDirectory(directory);
            }
            Map<String, String> env = cmd.getEnvironment();
            env.put("GIT_TERMINAL_PROMPT", "0");
            
            // Mask credentials in log if URL contains them
            String logCommand = String.join(" ", command).replaceAll("://.*@(.*)", "://$1");
            LOG.info("Executing: " + logCommand);

            CapturingProcessHandler processHandler = new CapturingProcessHandler(cmd);
            ProcessOutput output = processHandler.runProcess(TIMEOUT_MS);

            if (output.isTimeout()) {
                LOG.warn("Git command timed out: " + logCommand);
                return false;
            }
            if (output.getExitCode() != 0) {
                LOG.warn("Git command failed: " + logCommand + "\nExit Code: " + output.getExitCode() + "\nStderr: " + output.getStderr());
                return false;
            }
            return true;
        } catch (Exception e) {
            LOG.warn("Error executing Git command", e);
            return false;
        }
    }
}
