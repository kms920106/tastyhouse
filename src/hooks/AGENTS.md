<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-06-16 | Updated: 2026-06-16 -->

# hooks

## Purpose
Reusable client-side React hooks shared across routes.

## Key Files
| File | Description |
|------|-------------|
| `use-mobile.ts` | `useIsMobile()` — tracks a mobile breakpoint via `matchMedia`; drives responsive sidebar/layout behavior |
| `use-lg.ts` | Tracks the `lg` breakpoint via `matchMedia` for large-screen layout decisions |

## For AI Agents

### Working In This Directory
- Hooks are client-only (`"use client"` consumers); they use `window.matchMedia`, so guard for SSR if reused elsewhere.
- Add new shared hooks here; keep route-specific hooks in that route's `_components/`.

### Common Patterns
- Breakpoint hooks return a boolean and subscribe/unsubscribe to a media query listener in `useEffect`.

<!-- MANUAL: Any manually added notes below this line are preserved on regeneration -->
