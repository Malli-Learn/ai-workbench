# Security

- Uses the system Git executable and existing developer credentials.
- Sets `GIT_TERMINAL_PROMPT=0` for Git commands.
- Rejects HTTPS repository URLs with embedded credentials.
- Stores the catalog clone under `PathManager.getSystemPath()`.
- Stores install metadata under `PathManager.getConfigPath()/digital-ai-workbench/installed.json`.
- Rejects marketplace path traversal, plugin-name mismatches, duplicate plugin names, and symlink escapes.
- Never modifies the active IntelliJ project.
- Never writes `project/.github/agents` or `project/.github/skills`.
- Never executes skill scripts during installation.
- Refuses to overwrite unmanaged files under `~/.copilot/agents/` or `~/.copilot/skills/`.
- Retains manually modified managed files and blocks update/removal of those files.
