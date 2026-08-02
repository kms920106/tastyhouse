<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-06-16 | Updated: 2026-06-16 -->

# app

## Purpose
The Next.js **App Router** root. Defines the root layout/metadata, global styles, the 404 page, and two top-level route groups: `(external)` for public/landing routes and `(main)` for the authenticated app shell (auth screens, dashboards, chat, mail).

## Key Files
| File | Description |
|------|-------------|
| `layout.tsx` | Root layout — sets `<html>`, fonts (`src/lib/fonts`), theme bootstrap, providers, and metadata from `src/config/app-config.ts` |
| `globals.css` | Global Tailwind v4 layers and base CSS variables (default shadcn neutral theme) |
| `not-found.tsx` | Root 404 page |
| `favicon.ico` | App favicon |

## Subdirectories
| Directory | Purpose |
|-----------|---------|
| `(external)/` | Public routes outside the app shell (see `(external)/AGENTS.md`) |
| `(main)/` | Authenticated app: auth, dashboard, chat, mail (see `(main)/AGENTS.md`) |

## For AI Agents

### Working In This Directory
- **Route groups** `(...)` organize routes without affecting the URL path — `(external)` and `(main)` do not appear in URLs.
- Each route folder owns a `page.tsx` (and optional `layout.tsx`) plus a colocated `_components/` folder; the `_` prefix keeps that folder out of the route tree.
- Root-level concerns (fonts, theme boot, providers, metadata) belong in `layout.tsx`; do not duplicate them in child layouts.

### Common Patterns
- Server Components by default; add `"use client"` only where interactivity/state is needed.
- Dynamic catch-all routes use the `[...segment]` convention (e.g. dashboard's `[...not-found]`).

## Dependencies

### Internal
- `src/lib/fonts`, `src/config/app-config.ts`, `src/stores/preferences` (providers), `src/styles`

### External
- `next`, `react`, `next-themes`

<!-- MANUAL: Any manually added notes below this line are preserved on regeneration -->
