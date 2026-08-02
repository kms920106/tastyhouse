<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-06-16 | Updated: 2026-06-16 -->

# auth

## Purpose
Authentication UI screens. Provides **two layout variants** — `v1` and `v2` — each with `login` and `register` routes, sharing form and social-auth components. These are presentation-only (no real authentication/session in this template).

## Subdirectories
| Directory | Purpose |
|-----------|---------|
| `v1/` | Auth layout variant 1 — contains `login/` and `register/` pages |
| `v2/` | Auth layout variant 2 — contains `login/` and `register/` pages |
| `_components/` | Shared auth components (see below) |

### `_components/`
| File / Dir | Description |
|------------|-------------|
| `login-form.tsx` | Shared login form (react-hook-form + zod) used by both variants |
| `register-form.tsx` | Shared register form |
| `social-auth/` | Social login buttons/providers UI |

## For AI Agents

### Working In This Directory
- Form validation uses `react-hook-form` + `zod` via `@hookform/resolvers`. Define schemas with zod and wire through the resolver.
- The two variants (`v1`/`v2`) differ in layout/styling but reuse the same form components in `_components/` — change the form once, both variants update.
- No backend: submit handlers are stubs. Wire real auth via `src/server/server-actions.ts` when integrating.

## Dependencies

### External
- `react-hook-form`, `zod`, `@hookform/resolvers`, `lucide-react`, `simple-icons` (social buttons)

### Internal
- `src/components/ui/*` (form primitives)

<!-- MANUAL: Any manually added notes below this line are preserved on regeneration -->
