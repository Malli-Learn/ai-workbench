package com.digitalai.workbench.util;

import java.io.File;

public interface GitCommandExecutor {
    boolean execute(File directory, String... command);
}
