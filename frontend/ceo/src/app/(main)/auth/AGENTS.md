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
- `login/` 은 실제 백엔드(`/api/auth/v1/login`)에 연결된 운영 로그인 화면이다. `login-form.tsx` 는 `@/feature/auth/actions` 의 `loginAction` 을 호출하고, 실패 시 토스트로 메시지를 노출하며 성공 시 `callbackUrl`(미인증 접근 시 `src/proxy.ts` 가 붙여준다) 또는 `/dashboard/shop` 으로 이동한다.
- `v1/`·`v2/` 의 login/register 는 템플릿 잔재로 실제 인증에 연결되어 있지 않다 — 인증 동작을 바꿀 때는 `login/` 과 `_components/login-form.tsx` 를 기준으로 삼는다.

## Dependencies

### External
- `react-hook-form`, `zod`, `@hookform/resolvers`, `lucide-react`, `simple-icons` (social buttons)

### Internal
- `src/components/ui/*` (form primitives)

<!-- MANUAL: Any manually added notes below this line are preserved on regeneration -->
