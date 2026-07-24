# Digital AI Workbench IntelliJ Plugin

Offline-installable IntelliJ plugin for browsing the Digital AI Workbench marketplace and installing GitHub Copilot personal agents and skills.

Plugin ID: `com.digitalai.workbench.intellij`
Version: `0.1.0`

## Prerequisites
- IntelliJ IDEA 2025.3+
- GitHub Copilot for JetBrains, required to use installed agents and skills
- System `git` with existing credentials for the configured marketplace URL

## Build
From this directory:

```bash
./gradlew clean test
./gradlew verifyPlugin
./gradlew buildPlugin
```

The offline ZIP is written to `build/distributions/digital-ai-workbench-intellij-0.1.0.zip`.

## Install
IntelliJ -> Settings / Preferences -> Plugins -> Gear icon -> Install Plugin from Disk -> select the generated ZIP -> Restart IntelliJ.

Open the tool window at View -> Tool Windows -> Digital AI Workbench.

## Marketplace URL Configuration
Open Settings -> Tools -> Digital AI Workbench.

Default marketplace URL: `https://github.com/Malli-Learn/ai-workbench.git`

Enterprise GitLab example: `https://gitlab.yourcompany.com/YOUR-GROUP/ai-workbench.git`

## Install for Me Flow
The tool window shows plugin name, description, category, available version, installed version, and install status. Install for Me copies agents to `~/.copilot/agents/` and complete skill directories to `~/.copilot/skills/`.

## Update and Uninstall
Update and uninstall are available from the tool window. Managed files are checksum-verified; manually modified managed files are retained and block destructive changes.

## Docs
- [Architecture](docs/ARCHITECTURE.md)
- [Build](docs/BUILD.md)
- [Installation](docs/INSTALLATION.md)
- [Marketplace Connection](docs/MARKETPLACE-CONNECTION.md)
- [Security](docs/SECURITY.md)
- [Testing](docs/TESTING.md)

## Limitations
- Does not use undocumented GitHub Copilot APIs.
- Does not execute skill scripts during installation.
- Does not publish to JetBrains Marketplace.
