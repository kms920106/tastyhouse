<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-06-16 | Updated: 2026-06-22 -->

# src

## Purpose
Application source root. Uses a **colocation-based architecture**: routes live under `app/` (each feature owns its pages, layouts, and a local `_components/` folder), while cross-cutting concerns (shared UI, hooks, config, state, styles, data-access) live in dedicated top-level folders here.

## Key Files
| File | Description |
|------|-------------|
| `proxy.ts` | 활성 Next.js 16 Proxy(구 middleware). 인증 게이트: `/auth/*` 는 통과, accessToken 쿠키가 있으면 통과, 없고 refreshToken 만 있으면 `/api/auth/v1/refresh` 로 자동 갱신 후 새 쿠키를 응답에 실어 통과시킨다. 갱신 실패·refreshToken 부재면 인증 쿠키를 모두 지우고 `callbackUrl` 을 붙여 로그인으로 리다이렉트한다. Server Component 는 쿠키를 쓸 수 없어 자동 갱신은 반드시 이 지점에서 해야 한다 |
| `proxy.disabled.ts` | 템플릿 원본의 비활성 proxy 스텁 — 현재 라우팅에 영향을 주지 않는다. 인증 동작을 바꿀 때는 `proxy.ts` 를 수정한다 |

## Subdirectories
| Directory | Purpose |
|-----------|---------|
| `app/` | Next.js App Router routes, layouts, and route-local components (see `app/AGENTS.md`) |
| `components/` | Shared, app-wide components incl. the shadcn/ui primitive library (see `components/AGENTS.md`) |
| `config/` | App-level configuration constants (see `config/AGENTS.md`) |
| `lib/` | Utilities, fonts registry, and the preferences (theme/layout) engine (see `lib/AGENTS.md`) |
| `hooks/` | Reusable React hooks (see `hooks/AGENTS.md`) |
| `api/` | Data-access layer: resource repositories, DTOs, and shared HTTP client/types (see `api/AGENTS.md`) |
| `feature/` | Feature modules: Server Actions, validation schemas, and user-facing messages grouped per domain (see `feature/AGENTS.md`) |
| `stores/` | Zustand client state stores and providers (see `stores/AGENTS.md`) |
| `navigation/` | Sidebar navigation definition (see `navigation/AGENTS.md`) |
| `server/` | Next.js Server Actions (see `server/AGENTS.md`) |
| `scripts/` | Build-time scripts (theme preset generation) (see `scripts/AGENTS.md`) |
| `styles/` | Global theme presets and flag-icon CSS (see `styles/AGENTS.md`) |

## For AI Agents

### Working In This Directory
- Import using the `@/` alias (e.g. `@/lib/utils`, `@/components/ui/button`), which maps to `src/`.
- Place a component in a route's `_components/` only if it is used by that route alone; promote to `src/components/` when shared.
- Keep generated shadcn/ui primitives in `components/ui/` — install new ones via the shadcn CLI rather than hand-writing.

### Testing Requirements
- No unit tests. Verify with `npm run check` and `npm run build` from the repo root.

### Common Patterns
- Client components opt in with `"use client"`; default to Server Components.
- Theme/layout preferences flow through `lib/preferences/` + `stores/preferences/` and are persisted in cookies/localStorage.

## Dependencies

### External
- See root `AGENTS.md` for the full dependency list.

<!-- MANUAL: Any manually added notes below this line are preserved on regeneration -->
