# Testing

Run from `intellij-plugin/`:

```bash
./gradlew clean test
./gradlew verifyPlugin
./gradlew buildPlugin
```

Current unit coverage includes:

- Marketplace parsing.
- Plugin manifest parsing.
- Invalid paths.
- Duplicate plugins.
- Plugin-name mismatches.
- Git command construction.
- Agent copying.
- Recursive skill copying, including templates and scripts.
- Unmanaged collisions.
- Shared skills.
- Modified managed files.
- Updates blocked by modified files.
- Uninstall ownership behavior.

Network tests are not run by default.
