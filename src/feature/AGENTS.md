<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-06-22 | Updated: 2026-06-22 -->

# feature

## Purpose
Feature modules grouped by business domain. Each module bundles the server-side and validation concerns a feature needs — Server Actions, Zod schemas, and user-facing message constants — sitting between the route UI (`src/app/`) and the data-access layer (`src/api/`). Routes call these actions; the actions call the matching repository in `src/api/<resource>/`.

## Subdirectories
| Directory | Purpose |
|-----------|---------|
| `auth/` | 점주(CEO) 인증 feature: 로그인/토큰 갱신/로그아웃 Server Actions. 토큰은 `@/lib/auth-config` 의 `AUTH_COOKIE_KEYS` 쿠키에 저장하며, 만료기간은 `AUTH_SESSION_MAX_AGE`(rememberMe 30일 / 기본 7일)를 따른다. 갱신 API 응답에는 rememberMe 가 없으므로 `rememberMe` 쿠키를 함께 저장해 갱신 후에도 원래 세션 길이를 유지한다 — 세 쿠키는 항상 `persistSession()` 으로 함께 쓰고 로그아웃 시 함께 지운다. 로그인 429(Rate Limit)는 401 과 구분해 `LOGIN_RATE_LIMITED` 메시지로 안내한다. 토큰 쿠키는 `httpOnly` 로 심어 클라이언트 JS 에서 읽을 수 없다. 갱신 실패 시에는 쿠키를 전부 지워(`clearSession()`) 무한 재시도를 막는다. 실제 자동 갱신은 `src/proxy.ts` 가 수행한다 — Server Component 에서는 쿠키를 쓸 수 없어 `ApiClient` 가 갱신을 담당할 수 없기 때문이다. |
| `notice/` | Notice feature: CRUD Server Actions, form schema, and feedback messages (see `notice/AGENTS.md`) |
| `shop/` | Shop feature, **점주(shop owner)** scope: per-setting-item Server Actions, Zod schemas, enum catalog, pure time helpers (`time.ts`), and Korean copy backing `/dashboard/shop` (기본정보/운영정보 탭) and `/dashboard/shop-status` (전체현황·임시중지). See `shop/AGENTS.md`. The backend contract is still in progress and is confined to `@/api/shop/shop.dto.ts` + `shop.repository.ts`; this module depends only on `shop/domain`. `DAY_TYPE_*`/`CLOSED_DAY_TYPE_*`/`AMENITY_*`/`ORDER_METHOD_*` are real backend enums (2026-07-19 확인), the rest are PDF-derived — prefer the server's Korean label over `*_LABEL` when rendering existing items. |

## For AI Agents

### Working In This Directory
- One folder per domain. A typical module exports: `actions.ts` (`"use server"` actions), `schema.ts` (Zod validation + inferred types), and `message.ts` (Korean user-facing toast/error strings).
- Actions import DTOs and repositories from `@/api/<resource>/`; they must not embed raw HTTP/fetch calls — that belongs in the repository.
- After a successful mutation, call `revalidatePath()` for the affected route so server-rendered lists refresh.
- Keep all user-visible copy in `message.ts` (Korean), not inline in actions — consumers reference the constants.

### Common Patterns
- Actions return a small result object (e.g. `{ success, message?, id? }` / `{ success, data? }`) rather than throwing across the server boundary; the UI branches on `success`.
- Validate inputs with the module's Zod schema before touching the repository; on failure return the `INVALID_INPUT` message.

## Dependencies

### Internal
- `src/api/<resource>/` — repositories and DTOs the actions delegate to
- `src/app/(main)/dashboard/<feature>/` — route UI that invokes these actions

### External
- `zod` — schema validation; `next/cache` — `revalidatePath`

<!-- MANUAL: Any manually added notes below this line are preserved on regeneration -->
