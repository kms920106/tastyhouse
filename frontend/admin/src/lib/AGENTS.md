<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-06-16 | Updated: 2026-06-16 -->

# lib

## Purpose
Shared utilities and the client-side persistence/preferences engine. Holds the `cn()` class-name helper, browser storage wrappers, the font registry, and the theme/layout preferences logic that powers the customizable dashboard chrome.

## Key Files
| File | Description |
|------|-------------|
| `utils.ts` | `cn()` (clsx + tailwind-merge), `getInitials()`, and `formatCurrency()` (Intl-based) |
| `cookie.client.ts` | Client-side cookie read/write helpers (used to persist preferences server-readably) |
| `local-storage.client.ts` | Client-side localStorage read/write helpers |

## Subdirectories
| Directory | Purpose |
|-----------|---------|
| `fonts/` | Font registry — central declaration of the app's fonts (see `fonts/AGENTS.md`) |
| `preferences/` | Theme + layout preference types, defaults, storage, and utilities (see `preferences/AGENTS.md`) |

## For AI Agents

### Working In This Directory
- `*.client.ts` files contain browser-only APIs (cookies/localStorage) — import them only from client components or client-side code paths.
- Always compose Tailwind classes with `cn()` so conflicting utilities merge correctly.

### Common Patterns
- Pure, dependency-light helpers; no React in `utils.ts`.

## Dependencies

### External
- `clsx`, `tailwind-merge` (in `utils.ts`)

### Internal
- `preferences/` is consumed by `src/stores/preferences/` and the dashboard sidebar/layout controls.

<!-- MANUAL: Any manually added notes below this line are preserved on regeneration -->
