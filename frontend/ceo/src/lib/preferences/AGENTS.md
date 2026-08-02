<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-06-16 | Updated: 2026-06-16 -->

# preferences

## Purpose
The theme + layout preferences engine: type definitions, defaults, persistence, and helpers that drive the dashboard's customizable chrome (color preset, light/dark mode, sidebar collapse state, content width, etc.). Consumed by the Zustand preferences store and the sidebar layout controls.

## Key Files
| File | Description |
|------|-------------|
| `preferences-config.ts` | Master config: available options/defaults for preferences |
| `theme.ts` | Theme preference types (color presets, modes) |
| `theme-utils.ts` | Helpers to apply/resolve theme values |
| `layout.ts` | Layout preference types (sidebar variant, content width, etc.) |
| `layout-utils.ts` | Helpers to apply/resolve layout values |
| `preferences-storage.ts` | Reads/writes preferences via cookies/localStorage so they survive reloads and are server-readable |

## For AI Agents

### Working In This Directory
- When adding a new preference: extend the type in `theme.ts`/`layout.ts`, add its default to `preferences-config.ts`, wire persistence in `preferences-storage.ts`, and surface it in `stores/preferences/` + the sidebar `layout-controls.tsx`.
- Persistence layer relies on `src/lib/cookie.client.ts` and `src/lib/local-storage.client.ts`.

### Common Patterns
- Config-driven: defaults and option sets are centralized in `preferences-config.ts` rather than scattered.

## Dependencies

### Internal
- `src/lib/cookie.client.ts`, `src/lib/local-storage.client.ts`
- Consumed by `src/stores/preferences/` and the dashboard sidebar (`theme-switcher.tsx`, `layout-controls.tsx`).

### External
- `next-themes` (mode handling)

<!-- MANUAL: Any manually added notes below this line are preserved on regeneration -->
