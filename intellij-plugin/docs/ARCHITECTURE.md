# Architecture

The plugin keeps the IntelliJ project untouched. It clones the marketplace into the IntelliJ system cache and installs personal Copilot customizations under the user's home directory.

## Packages
- `com.digitalai.workbench.catalog`: Git catalog clone/refresh, marketplace parsing, plugin validation, category inference.
- `com.digitalai.workbench.install`: checksum calculation, install registry, staged copying, update, uninstall, shared skill ownership.
- `com.digitalai.workbench.settings`: persistent settings page under Settings -> Tools -> Digital AI Workbench.
- `com.digitalai.workbench.ui`: right-side tool window, filtering, status display, install/update/uninstall actions.
- `com.digitalai.workbench.util`: Git command execution with `GIT_TERMINAL_PROMPT=0`.

Git, parsing, install, update, and uninstall work run from IntelliJ background tasks. Swing updates are marshalled back to the UI thread.
