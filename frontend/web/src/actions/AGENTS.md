<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-06-05 | Updated: 2026-06-05 -->

# actions

## Purpose
`'use server'` 경계 어댑터입니다. client component ↔ server-only 도메인 사이를 잇는 **얇은** 계층으로, 내부에서 `@/domains/[domain]`의 repository/service를 호출해 결과를 반환합니다. 도메인 로직(HTTP·가공·조합)은 들어오지 않습니다.

> **📖 작성 표준은 [`CLAUDE.md`](./CLAUDE.md)에 상세히 정의되어 있습니다. server action을 만들거나 수정하기 전에 반드시 읽으세요.** Best Practice 레퍼런스는 `place.ts`입니다.

## Key Files
| File | Description |
|------|-------------|
| `CLAUDE.md` | **Server Actions 작성 표준 가이드** (필독) |
| `place.ts` | **레퍼런스** — 순수 read wrapper(패턴 A), unwrap(패턴 B) |
| `auth.ts` / `signup.ts` (auth) | FormData + cookies + redirect (패턴 D) |
| `review.ts` / `rank.ts` | mutation + `revalidatePath` (패턴 C) |

`[domain].ts` 파일이 도메인마다 1개씩 존재합니다 (`banner`, `bug-report`, `email-verification`, `event`, `faq`, `follow`, `member`, `notice`, `order`, `partnership`, `payment`, `phone-verification`, `policies`, `product`, `reservation`, `search`, `shop`).

## For AI Agents

### Working In This Directory
- 첫 줄은 항상 `'use server'`, 모든 export는 `async function`
- **1 도메인 = 1 파일** (`[domain].ts`)
- repository/service는 **명시 경로**(`@/domains/[domain]/[domain].repository`)로 import — barrel 금지
- 타입/DTO/constants는 barrel(`@/domains/[domain]`)에서 import 가능
- 파라미터 타입은 **인라인 객체 타입**으로 선언 — 도메인 DTO/`PaginationParams`를 파라미터 타입으로 노출 금지 (§8.8)
- `revalidatePath`는 **성공 분기에서만** 호출
- `api.*` 직접 호출·가공(`map`/`filter`) 금지 → repository/service로
- `try/catch`로 에러 삼키기 금지 (repository가 이미 `{ error }` 반환)

### 4가지 표준 패턴
- **A** 순수 read wrapper (70%+) / **B** unwrap (`?? []`) / **C** mutation + revalidate / **D** FormData + cookies + redirect (`FormAction` suffix, discriminated union 결과 타입)

## Dependencies

### Internal
- `@/domains/[domain]/[domain].repository` · `[domain].service`
- `@/lib/auth-config` (쿠키 키)

### External
- `next/cache` (`revalidatePath`), `next/navigation` (`redirect`), `next/headers` (`cookies`)

<!-- MANUAL: -->
