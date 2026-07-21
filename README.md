# Digital AI Workbench Marketplace

## Purpose
Enterprise AI agents and skills for business, engineering, architecture, Java, and React Native workflows.

## Category Structure
The plugins are organized into specific workflow categories to ensure clarity and logical separation of capabilities.

## Available Plugins

| Plugin Name | Category | Version | Status |
|---|---|---|---|
| jira-story-generator | Business Workflows | 0.1.0 | Initial scaffold |
| pull-request-description | Engineering Workflows | 0.1.0 | Initial scaffold |
| release-notes-generator | Engineering Workflows | 0.1.0 | Initial scaffold |
| java-unit-test-coverage | Java Engineering | 0.1.0 | Initial scaffold |
| java-jacoco-analysis | Java Engineering | 0.1.0 | Initial scaffold |
| java-code-cleanup | Java Engineering | 0.1.0 | Initial scaffold |
| react-native-unit-test-coverage | React Native Engineering | 0.1.0 | Initial scaffold |
| react-native-code-cleanup | React Native Engineering | 0.1.0 | Initial scaffold |
| technical-design-generator | Architecture Workflows | 0.1.0 | Initial scaffold |
| architecture-diagram-generator | Architecture Workflows | 0.1.0 | Initial scaffold |
| code-documentation-generator | Architecture Workflows | 0.1.0 | Initial scaffold |

## Independently Installable
Each plugin listed above is independently installable. You can browse the marketplace and choose only the plugins necessary for your work.

## Categories Explained
- **Business Workflows**: Tools for gathering requirements, defining stories, and aligning business goals.
- **Engineering Workflows**: Tools for managing pull requests, code reviews, and release cycles.
- **Java Engineering**: Tools tailored specifically for Java code generation, testing, and analysis.
- **React Native Engineering**: Tools designed for React Native mobile app development and testing.
- **Architecture Workflows**: Tools to document technical designs, generate architecture diagrams, and analyze structure.

## Local VS Code Testing
Please see `docs/vscode-local-testing.md` for full instructions.

## Copilot CLI Testing
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
*(After changing a locally installed plugin, it may need to be installed again so the cached copy is refreshed.)*

> **Note:** All current agents and skills in this repository are initial placeholders.
