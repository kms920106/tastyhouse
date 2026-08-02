<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-06-05 | Updated: 2026-06-05 -->

# src

## Purpose
애플리케이션 소스 전체입니다. 라우팅(`app/`), 도메인 비즈니스 계층(`domains/`), server action 경계(`actions/`), 공용 UI(`components/`), 순수 유틸(`lib/`)이 명확히 분리되어 있습니다. 데이터 흐름은 **컴포넌트 → (client) domain hook → server action → repository/service → `lib/api` → Backend** 또는 **(server) repository 직접 호출**입니다.

## Key Files
| File | Description |
|------|-------------|
| `middleware.ts` | 요청 단위 토큰 검증·갱신, 보호 라우트 redirect |

## Subdirectories
| Directory | Purpose |
|-----------|---------|
| `app/` | Next.js App Router 라우팅·페이지 (see `app/AGENTS.md`, **`app/CLAUDE.md`**) |
| `domains/` | 도메인별 7-Layer 비즈니스 계층 (see `domains/AGENTS.md`, **`domains/CLAUDE.md`**) |
| `actions/` | `'use server'` 경계 어댑터 (see `actions/AGENTS.md`, **`actions/CLAUDE.md`**) |
| `components/` | 라우트 간 공용 UI 컴포넌트 (see `components/AGENTS.md`) |
| `lib/` | 순수 유틸·API 클라이언트·경로 상수 (see `lib/AGENTS.md`) |
| `hooks/` | 도메인 무관 범용 client hook (see `hooks/AGENTS.md`) |
| `constants/` | 전역 상수 (에러 메시지, 검증 규칙) (see `constants/AGENTS.md`) |
| `types/` | 전역 타입 (`PaginationParams` 등) (see `types/AGENTS.md`) |
| `providers/` | React Context Provider (QueryProvider) (see `providers/AGENTS.md`) |
| `styles/` | 전역 CSS (Tailwind 진입점) (see `styles/AGENTS.md`) |

## For AI Agents

### Working In This Directory
- **계층 경계를 지키세요.** `domains/`의 `repository`/`service`는 `import 'server-only'`이므로 client component에서 import하면 빌드 에러가 발생합니다.
- **`@tanstack/react-query`를 `app/`·`components/`에서 직접 import 금지** — 반드시 `domains/[domain]/[domain].hook.ts`를 경유합니다.
- 외부 레이어 파일을 수정할 때는 **해당 레이어의 CLAUDE.md를 먼저 읽으세요** (`app/`, `domains/`, `actions/`에 존재).

### Common Patterns
- 경로 alias `@/` = `src/`
- 도메인 public API는 barrel(`@/domains/[domain]`)로 접근 (client-safe layer만 export)

## Dependencies

### Internal
- `app/` → `actions/` → `domains/` → `lib/api` (단방향)
- 모든 계층이 `lib/`, `constants/`, `types/`를 공유

<!-- MANUAL: -->
