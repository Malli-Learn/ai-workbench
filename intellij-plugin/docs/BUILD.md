# Build

## Requirements
- JDK 24 installed locally for Gradle execution.
- Java 21 bytecode target via `--release 21`.
- Gradle wrapper `9.2.0`.
- IntelliJ Platform Gradle Plugin `2.18.1`.
- IntelliJ IDEA 2025.3.6 compatible verification target, build `253.33813.25`.

## Commands
Run from `intellij-plugin/`:

```bash
./gradlew clean test
./gradlew verifyPlugin
./gradlew buildPlugin
```

The offline ZIP is produced at:

```text
build/distributions/digital-ai-workbench-intellij-0.1.0.zip
```

`verifyPlugin` mutes `TemplateWordInPluginId` because the required plugin ID is `com.digitalai.workbench.intellij`.
