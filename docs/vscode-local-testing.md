# VS Code Local Testing Guide

1. Open the repository in VS Code.
2. Open VS Code User Settings JSON.
3. Add the absolute local repository path as a file URI:

```json
{
  "chat.plugins.marketplaces": [
    "file:///ABSOLUTE/PATH/TO/digital-ai-workbench"
  ]
}
```

4. Do not use `${workspaceFolder}` in this setting.
5. Run `Developer: Reload Window`.
6. Open the Extensions view.
7. Search for:

```text
@agentPlugins
```

8. Verify that every marketplace entry appears as a separate installable plugin.
9. Install one plugin.
10. Accept the marketplace trust prompt when the source is correct.
11. Open Copilot Chat.
12. Verify the installed custom agent appears in the agent selector.
13. Verify the included skill appears in the skills configuration.
14. Document troubleshooting checks:

    * Use a current VS Code version.
    * Confirm GitHub Copilot is installed and authenticated.
    * Confirm agent plugins are enabled by enterprise policy.
    * Validate `marketplace.json`.
    * Validate every `source` path.
    * Validate every `plugin.json`.
    * Confirm names use lowercase kebab-case.
    * Reload VS Code after changes.

## Copilot CLI testing guide
```bash
copilot plugin marketplace add /ABSOLUTE/PATH/TO/digital-ai-workbench
copilot plugin marketplace list
copilot plugin marketplace browse digital-ai-workbench
```

For an individual local plugin test, include:
```bash
copilot plugin install ./plugins/java-engineering/java-unit-test-coverage
copilot plugin list
```

Also explain that after changing a locally installed plugin, it may need to be installed again so the cached copy is refreshed.
