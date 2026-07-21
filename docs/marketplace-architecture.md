# Marketplace Architecture

## Purpose
This document outlines the architecture for the Digital AI Workbench plugin marketplace.

## Repository Architecture
The repository holds a centralized marketplace manifest (`.github/plugin/marketplace.json`) and a collection of independent plugins organized by domain categories under the `plugins/` directory. 

## Marketplace versus Plugin Distinction
The marketplace is a catalog that points to multiple plugins. A plugin is an independent package that provides one or more custom agents and skills.

## Category Folders versus Installable Plugins
Plugins are grouped into category folders (e.g., `business-workflows`, `engineering-workflows`) for organization. However, the installable unit is the plugin itself (e.g., `jira-story-generator`), not the category folder.

## Rule: Each Marketplace Entry Produces One Installable Plugin
Every entry defined in the marketplace manifest maps directly to exactly one installable plugin.

## Rule: Agents/Skills Do Not Become Separate Install Cards
An agent or skill inside a plugin does not automatically become a separate marketplace install card. The plugin encapsulates them.

## Naming Conventions
All marketplace names, plugin names, skill names, directory names, and filenames use lowercase kebab-case.

## Versioning Approach
All plugins start at version `0.1.0`. Versions are semantic and must match between the marketplace manifest and the `plugin.json` of the respective plugin.

## Future Governance Placeholders
- Automated linting for plugins.
- Review and approval requirements.
- Integration tests for agents and skills.
