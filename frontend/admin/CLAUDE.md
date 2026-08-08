# CLAUDE.md (tastyhouse-admin)

> AI 규칙, GIT 규칙, 플랜 작성 규칙, 네이밍 규칙, E2E 검증 규칙 등 리포 전체 공통 규칙은 **리포지토리 루트의 `CLAUDE.md`** 를 참조합니다. 이 파일은 admin 앱 고유 내용만 다룹니다.

## Commands

아래 명령은 모두 `frontend/admin`(리포 루트 기준) 디렉터리에서 실행합니다.

```bash
npm run dev          # dev server on port 3010
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
