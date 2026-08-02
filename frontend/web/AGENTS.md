<!-- Generated: 2026-06-05 | Updated: 2026-06-05 -->

# tastyhouse-web

## Purpose
맛집 예약·주문·리뷰 모바일 웹 서비스의 프론트엔드입니다. Next.js 15 App Router 기반으로, Server Component 우선 렌더링과 도메인 계층 분리(7-Layer) 아키텍처를 따릅니다. 인증은 httpOnly 쿠키(accessToken/refreshToken), 결제는 TossPayments SDK를 사용합니다.

## Key Files
| File | Description |
|------|-------------|
| `package.json` | 의존성 및 스크립트 (`dev`, `build`, `lint`, `e2e` 등) |
| `next.config.ts` | Next.js 설정 |
| `middleware.ts` (in `src/`) | 토큰 검증·갱신, 보호 라우트 처리 |
| `tsconfig.json` | TypeScript 설정 (`@/*` → `src/*` alias) |
| `playwright.config.ts` | E2E 테스트 설정 (Page Object Model) |
| `eslint.config.mjs` / `prettier.config.cjs` | 린트·포맷 설정 |
| `components.json` | shadcn/ui 설정 |
| `instrumentation.ts` / `instrumentation-client.ts` | 런타임 계측 (로깅 등) |
| `CLAUDE.md` | AI 작업 규칙 (한국어 답변, `npm run build` 금지, `NO_COMMIT_OR_ROLLBACK`) |

## Subdirectories
| Directory | Purpose |
|-----------|---------|
| `src/` | 애플리케이션 소스 (see `src/AGENTS.md`) |
| `e2e/` | Playwright E2E 테스트 (see `e2e/AGENTS.md`) |
| `public/` | 정적 자산 (폰트, 이미지) |
| `.github/workflows/` | CI (E2E 워크플로 `e2e.yml`) |
| `md/` | 문서 |

## For AI Agents

### Working In This Directory
- **답변은 한국어로 작성합니다.**
- 로직 구현 후 `npm run build`를 실행하지 않습니다 (`CLAUDE.md` 규칙). 타입 검증이 필요하면 `npm run type-check`를 사용하세요.
- **커밋·롤백 금지** (`NO_COMMIT_OR_ROLLBACK`). 사용자가 명시적으로 요청할 때만 git 작업을 수행합니다.
- 네이밍은 명확하고 의미 있는 이름을 선택합니다.

### Testing Requirements
- E2E: `npm run e2e` (Playwright). `npm run e2e:ui`로 UI 모드.
- 타입: `npm run type-check`
- 린트: `npm run lint` / `npm run lint:fix`
- 통합: `npm run code-quality` (lint:fix + format + type-check)

### Common Patterns
- 경로 alias `@/` = `src/`
- 페이지 경로 상수는 `src/lib/paths.ts`의 `PAGE_PATHS`로 중앙 관리

## Dependencies

### External
- **next 15** / **react 19** — App Router, Server Components
- **@tanstack/react-query** — client 데이터 페칭 (도메인 hook 경유)
- **@tosspayments/tosspayments-sdk** — 결제
- **radix-ui** / shadcn — UI 프리미티브
- **tailwindcss 4** — 스타일링
- **zod** — 검증
- **pino** — 구조화 로깅
- **@playwright/test** + **@axe-core/playwright** — E2E·접근성 테스트

<!-- MANUAL: Any manually added notes below this line are preserved on regeneration -->
