# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## AI 규칙

명령어에 대한 답변은 한국어로 하도록 합니다.

명령된 로직을 구현 후, gradle build 테스트는 진행하지 않도록 합니다.

브라우저 동작 확인이 필요한 작업(화면/폼/플로우 구현 및 수정)을 완료했을 때는, 코드 구현 → 빌드/린트 → 코드리뷰만으로 끝내지 말고 **직접 개발 서버를 기동해 MCP Playwright로 실제 화면 동작까지 검증**합니다. `npm run build`/`npm run check` 통과는 "테스트 스위트가 없으니 빌드가 최소 게이트"라는 의미일 뿐, 브라우저 검증을 생략해도 된다는 뜻이 아닙니다. 절차:

1. 개발 서버가 떠 있는지 확인하고, 없으면 `npm run dev`(3020 포트)를 백그라운드로 기동합니다.
2. 로그인이 필요하면 `.env.local`의 `E2E_USERNAME`/`E2E_PASSWORD`로 로그인합니다.
3. MCP Playwright 도구(`mcp__playwright__*`)로 변경된 화면의 정상 플로우(생성/수정/조회 등)와 주요 예외 케이스를 실제로 조작해 확인합니다.
4. 검증 결과(성공/실패, 스크린샷 필요 시)를 작업 완료 보고에 함께 명시합니다.

플랜(계획서)에 e2e 검증 단계가 명시되어 있다면 반드시 끝까지 수행하고, 시간이 부족하거나 서버 기동이 불가능한 등의 이유로 생략할 경우에는 반드시 그 사실과 이유를 명확히 알립니다.

## GIT 규칙

NO_COMMIT_OR_ROLLBACK

명령된 작업(리팩터링/기능 구현 등)이 끝나면, 커밋은 직접 실행하지 않되(NO_COMMIT_OR_ROLLBACK) **추천 커밋 메시지를 항상 함께 제시**합니다. 최근 커밋 로그(`git log`)의 컨벤션(`{type}({scope}): {한글 요약}` 형태, 예: `refactor(naming): ...`, `feat(point): ...`, `style(response): ...`)을 따르고, 본문은 무엇을 왜 바꿨는지(동작 변경 여부 포함) 한국어로 서술합니다. 신설 규칙이 있어 CLAUDE.md/AGENTS.md를 갱신한 경우 그 사실도 본문에 언급합니다.

사용자가 명시적으로 git commit 또는 git push를 요청한 경우에는, 현재 브랜치가 `prod`를 포함한 어떤 브랜치이든 별도로 확인 질문을 하지 않고 바로 진행합니다. 이 저장소는 현재 `prod` 브랜치를 기본으로 사용하며, 사용자가 커밋/푸시를 요청한 것 자체가 대상 브랜치에 대한 승인으로 간주합니다.

## 플랜 작성 규칙

작업 플랜을 작성할 때는, 이번 작업으로 인해 새로운 컨벤션이 생기거나 기존 규칙이 바뀌는지 확인하여 루트 및 각 모듈의 `CLAUDE.md`/`AGENTS.md` 문서도 갱신이 필요한지 함께 검토합니다.

브라우저 동작 확인이 필요한 작업(화면/폼/플로우 구현 및 수정)의 플랜을 작성할 때는, **구현 전에 MCP Playwright로 검증할 e2e 시나리오를 플랜에 반드시 명시**합니다. 이 시나리오는 사후 검증(위 "AI 규칙"의 MCP Playwright 검증 절차) 단계에서 그대로 수행 대상이 됩니다. 순수 백엔드/설정/리팩터링/문서 등 브라우저 조작이 필요 없는 작업은 이 항목이 면제됩니다. 시나리오는 다음을 포함합니다.

- **검증 대상 화면**: 이번 작업으로 신설·변경되는 화면/폼/플로우를 열거합니다.
- **정상 플로우**: 화면별 주요 정상 동작(생성/수정/조회/삭제 등)을 항목으로 구체적으로 기술합니다.
- **주요 예외 케이스**: 유효성 검증 실패, 권한/의존 데이터 부재, 빈 상태, 에러 토스트 등 확인해야 할 예외 상황을 함께 열거합니다.

시나리오에 명시된 항목은 "AI 규칙"의 검증 절차에 따라 끝까지 수행하며, 생략 시에는 그 사실과 이유를 명확히 알립니다.

## 네이밍 규칙

파일명, 변수명, 함수명 등 모든 네이밍은 최적의 이름을 선택하도록 합니다. 명확하고 의미 있는 이름을 사용하여 코드의 가독성과 유지보수성을 높입니다.

## Commands

```bash
npm run dev          # dev server on port 3020
npm run build         # production build — the real correctness gate; no test suite exists
npm run check         # Biome lint + format check
npm run check:fix     # Biome lint + format, auto-fix
npm run lint          # Biome lint only
npm run format        # Biome format, write
npm run test:e2e      # Playwright e2e tests
npm run test:e2e:ui   # Playwright e2e tests, UI mode
```

Run a single Playwright spec: `npx playwright test e2e/<file>.spec.ts`.

Husky + lint-staged run `biome check --write` on staged `.js/.ts/.jsx/.tsx` files pre-commit.

## E2E 테스트 (MCP Playwright)

브라우저 조작이 필요한 E2E 테스트/검증 작업은 `npx playwright test` 대신 **MCP Playwright 도구**(`mcp__playwright__*`)를 사용합니다. 로그인이 필요한 플로우는 아래 테스트 계정으로 로그인합니다.

- 테스트 계정 정보는 `.env.local`의 `E2E_USERNAME`, `E2E_PASSWORD`를 사용합니다.
- 개발 서버(`npm run dev`, 3020 포트)가 실행 중이어야 합니다.

## Architecture

Colocation-based Next.js 16 App Router app (mock data, no real backend). Each directory under `src/` has its own `AGENTS.md` with detailed, current guidance — **read the relevant one before working in that area** rather than relying on this summary. Root: `AGENTS.md`, then e.g. `src/api/AGENTS.md`, `src/feature/AGENTS.md`, `src/app/AGENTS.md`.

### Layering (request flow for a feature)

```
route UI (src/app/(main)/dashboard/<feature>/)
  → Server Action (src/feature/<domain>/actions.ts, "use server")
    → repository (src/api/<resource>/<resource>.repository.ts — pure HTTP, returns DTOs)
      → shared HTTP client (src/api/shared/client.ts)
```

- **`src/api/<resource>/`**: `*.repository.ts` (transport only, returns `*Response` DTOs), `*.dto.ts` (DTOs, never leave this layer), optionally `*.service.ts` (DTO → domain mapping, only when a query needs it). Mutations with no transformation are called directly on the repository from feature actions.
- **`src/feature/<domain>/`**: `actions.ts` (Server Actions, return `{ success, message?, data? }` instead of throwing — UI branches on `success`), `schema.ts` (Zod validation + inferred types), `message.ts` (Korean user-facing copy — never inline strings in actions), `constants.ts`, `domain.ts` (domain types imported by both UI and `api/*.service.ts`). After a mutation, actions call `revalidatePath()` for the affected route.
- **`src/app/(main)/dashboard/<feature>/`**: `page.tsx` (Server Component, reads searchParams, calls the service/repository for initial data) + `_components/` (route-local Client Components only used here). Promote a component to `src/components/` only once it's shared across routes.
- Dependency direction is one-way: `api/<resource>` may import domain types from `feature/<domain>/domain`, never the reverse.

### Conventions

- Import via the `@/` alias → `src/`.
- Client Components opt in with `"use client"`; default is Server Components.
- Forms: `react-hook-form` + `zodResolver` + `Controller` around shadcn `Select`/`Input`/`Textarea`. Keep a Radix `Select`'s `value` prop a stable type (string) across the component's lifetime — never let it flip between `undefined` and a string (e.g. use `field.value ?? ""` rather than `field.value ?? undefined`), or React warns about switching from uncontrolled to controlled.
- Tables: `@tanstack/react-table` with manual pagination driven by URL search params (`router.push` with `URLSearchParams`), not client-side pagination state.
- Keep shadcn/ui primitives in `src/components/ui/` generated via the shadcn CLI — don't hand-write replacements.
- Compose class names with `cn()` from `src/lib/utils.ts`.
