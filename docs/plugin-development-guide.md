# Plugin Development Guide

## How to add a new plugin
Create a new directory under the appropriate category inside the `plugins/` directory. The directory name must use lowercase kebab-case.

## How to add it to marketplace.json
Add a new object to the `plugins` array in `.github/plugin/marketplace.json`. Ensure the `name` matches the plugin directory, and the `source` points to the correct relative path.

## How to create plugin.json
Create a `plugin.json` inside the plugin directory. Use the standard structure:
```json
{
  "name": "<plugin-name>",
  "description": "<short plugin description>",
  "version": "0.1.0",
  "author": {
    "name": "Digital AI Workbench Team"
  },
  "agents": "agents/",
  "skills": "skills/"
}
```

## How to add an agent
Create an `<agent-name>.agent.md` file inside the `agents/` directory of your plugin. Include the required YAML frontmatter with `name` and `description`.

## How to add a skill
Create a directory inside `skills/` named `<skill-name>`, then create a `SKILL.md` inside it. Include the required YAML frontmatter with `name` and `description`.

## How to maintain matching names
Ensure the `name` in `plugin.json` matches the plugin's entry in `marketplace.json` and the plugin's directory name.

## How to test locally
Follow the instructions in `docs/vscode-local-testing.md`.

## How to increment versions
When updating a plugin, increment its `version` field in both its `plugin.json` and the `.github/plugin/marketplace.json` entry.

## Placeholder review checklist
- Valid JSON.
- Lowercase kebab-case naming.
- Matching versions.
- Required frontmatter in markdown files.
