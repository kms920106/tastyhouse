<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-06-16 | Updated: 2026-06-16 -->

# config

## Purpose
App-level configuration constants consumed across the UI (branding, metadata, version).

## Key Files
| File | Description |
|------|-------------|
| `app-config.ts` | Exports `APP_CONFIG` — app `name` ("Tastyhouse Admin"), `version` (read from `package.json`), `copyright` (with current year), and `meta.title`/`meta.description` used for HTML metadata |

## For AI Agents

### Working In This Directory
- This is a **hot path** — `app-config.ts` is referenced widely. Change branding strings here rather than hardcoding them in components.
- `version` is imported from `package.json`; bump the version in `package.json`, not here.

### Common Patterns
- Single exported `APP_CONFIG` object; extend it with new top-level keys rather than adding new files unless a concern is genuinely separate.

## Dependencies

### Internal
- Imports `../../package.json` for the version string.

<!-- MANUAL: Any manually added notes below this line are preserved on regeneration -->
