<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-06-16 | Updated: 2026-06-16 -->

# (main)

## Purpose
Route group for the core application: authentication screens, the dashboards, and the standalone chat and mail experiences, plus an unauthorized page. The group name `(main)` does not appear in URLs.

## Subdirectories
| Directory | Purpose |
|-----------|---------|
| `auth/` | Authentication screens (v1/v2 login & register), UI-only (see `auth/AGENTS.md`) |
| `dashboard/` | All dashboard feature routes + shared sidebar chrome (see `dashboard/AGENTS.md`) |
| `chat/` | Standalone chat page with its own layout and components (see `chat/AGENTS.md`) |
| `mail/` | Standalone mail/email page with its own layout and components (see `mail/AGENTS.md`) |
| `unauthorized/` | "Unauthorized" / access-denied page (RBAC placeholder) |

## For AI Agents

### Working In This Directory
- `chat/` and `mail/` exist both here (top-level standalone pages) and as routes inside `dashboard/`; check which one a change targets.
- Auth is presentation-only in this template (no real session). RBAC and multi-tenant are noted as planned, hence the `unauthorized/` page.

### Common Patterns
- Each feature is a route folder with `page.tsx`, optional `layout.tsx`, and colocated `_components/`.

<!-- MANUAL: Any manually added notes below this line are preserved on regeneration -->
