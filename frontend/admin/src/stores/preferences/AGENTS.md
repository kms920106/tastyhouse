<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-06-16 | Updated: 2026-06-16 -->

# preferences (store)

## Purpose
The Zustand store and provider for user preferences (theme preset, light/dark mode, sidebar/layout options). Hydrates from persisted values and exposes setters that the sidebar layout controls call.

## Key Files
| File | Description |
|------|-------------|
| `preferences-store.ts` | Zustand store: preference state + setter actions; uses `src/lib/preferences/` for types/defaults and persistence |
| `preferences-provider.tsx` | Client React provider that initializes the store (e.g. with server/cookie-derived initial values) and wraps the app subtree |

## For AI Agents

### Working In This Directory
- Initial preference values are typically passed in from a layout (read from cookies server-side) to avoid hydration flashes — keep that flow intact when editing the provider.
- New preferences must also be threaded through `src/lib/preferences/` (types, defaults, storage).

## Dependencies

### External
- `zustand`

### Internal
- `src/lib/preferences/` and `src/lib/cookie.client.ts` / `local-storage.client.ts`

<!-- MANUAL: Any manually added notes below this line are preserved on regeneration -->
