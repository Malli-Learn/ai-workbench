# Marketplace Connection

The default marketplace is:

```text
https://github.com/Malli-Learn/ai-workbench.git
```

Settings -> Tools -> Digital AI Workbench supports changing the HTTPS Git URL and branch/tag.

## GitHub

```bash
git ls-remote https://github.com/Malli-Learn/ai-workbench.git
```

## GitLab

```bash
git ls-remote https://gitlab.yourcompany.com/YOUR-GROUP/ai-workbench.git
```

Private GitLab credentials must already work through system Git, Git Credential Manager, enterprise SSO, VPN, and certificate trust.

The plugin sets `GIT_TERMINAL_PROMPT=0`, does not ask for usernames/passwords/tokens, and rejects embedded credentials in repository URLs.

If refresh fails, the tool window reuses the last successful catalog cache when available.
