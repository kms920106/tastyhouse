<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-06-16 | Updated: 2026-06-16 -->

# stores

## Purpose
Client-side global state using **Zustand**. Currently holds the user preferences store that backs theme/layout customization.

## Subdirectories
| Directory | Purpose |
|-----------|---------|
| `preferences/` | Preferences store + React context provider (see `preferences/AGENTS.md`) |

## For AI Agents

### Working In This Directory
- Add a new store as its own subfolder (`<feature>/<feature>-store.ts` + optional `<feature>-provider.tsx`), mirroring the `preferences/` pattern.
- Stores hold client state only; do not put server data fetching here.

## Dependencies

### External
- `zustand`

### Internal
- `src/lib/preferences/` (types, defaults, persistence)

<!-- MANUAL: Any manually added notes below this line are preserved on regeneration -->
