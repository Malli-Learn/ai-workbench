# VS Code Local Testing Guide

## VS Code Shortcuts

| Purpose                    | Shortcut or command                       |
| -------------------------- | ----------------------------------------- |
| Open Command Palette       | `Cmd + Shift + P`                         |
| Open Extensions            | `Cmd + Shift + X`                         |
| Edit marketplace URL       | `Preferences: Open User Settings (JSON)`  |
| Manage marketplaces        | `Chat: Manage Plugin Marketplaces`        |
| Check plugin updates       | `Extensions: Check for Extension Updates` |
| Reload VS Code             | `Developer: Reload Window`                |
| Search marketplace plugins | `@agentPlugins`                           |
| View customizations        | `Chat: Open Customizations`               |

*On Windows or Linux, use `Ctrl + Shift + P` for the Command Palette and `Ctrl + Shift + X` for Extensions.*

## Updating marketplace URLs

1. Press `Cmd + Shift + P` on macOS.
2. Run `Preferences: Open User Settings (JSON)`.
3. Add or update:
   ```json
   "chat.plugins.marketplaces": [
       "Malli-Learn/ai-workbench"
   ]
   ```
4. Save the file.
5. Run `Developer: Reload Window`.

## Managing plugin marketplaces

1. Press `Cmd + Shift + P`.
2. Run `Chat: Manage Plugin Marketplaces`.
3. Confirm that this marketplace is listed:
   `Malli-Learn/ai-workbench`
4. Use this command to browse, refresh, or remove configured marketplaces.

## Searching for plugins

1. Press `Cmd + Shift + X` to open Extensions.
2. In the Extensions search box, enter:
   `@agentPlugins`
3. Browse the plugins from Digital AI Workbench.
4. Select a plugin and click **Install**.

## Checking for plugin updates

After pushing new plugin changes or versions:

1. Press `Cmd + Shift + P`.
2. Run `Extensions: Check for Extension Updates`.
3. Wait for the marketplace refresh to finish.
4. Run `Developer: Reload Window`.
5. Open Extensions using `Cmd + Shift + X`.
6. Search again for:
   `@agentPlugins`

*VS Code also checks for plugin updates automatically, but the command triggers an immediate check.*

## Reloading VS Code

1. Press `Cmd + Shift + P`.
2. Run `Developer: Reload Window`.

Use this after:
* Adding a marketplace URL.
* Changing marketplace settings.
* Updating plugin versions.
* Installing or updating plugins.
* Fixing marketplace manifest errors.

## Installing a plugin

1. Press `Cmd + Shift + X`.
2. Search:
   `@agentPlugins`
3. Select the required plugin.
4. Click **Install**.
5. Accept the trust prompt after confirming the repository source.
6. Open Copilot Chat.
7. Select the installed custom agent from the agent selector.

## Viewing installed agents and skills

1. Open Copilot Chat.
2. Use the agent selector to find installed custom agents.
3. Press `Cmd + Shift + P`.
4. Run `Chat: Open Customizations`.
5. Review the installed agents, skills, prompts, and instructions.

## Recommended sequence after pushing changes

1. `Cmd + Shift + P`
   → `Extensions: Check for Extension Updates`
   → `Developer: Reload Window`

2. `Cmd + Shift + X`
   → Search `@agentPlugins`
   → Install or update the plugin

## Copilot CLI testing guide

```bash
copilot plugin marketplace add Malli-Learn/ai-workbench
copilot plugin marketplace list
copilot plugin marketplace browse digital-ai-workbench
```

For an individual local plugin test, include:
```bash
copilot plugin install ./plugins/java-engineering/java-unit-test-coverage
copilot plugin list
```
*(After changing a locally installed plugin, it may need to be installed again so the cached copy is refreshed.)*
