<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-06-05 | Updated: 2026-06-05 -->

# e2e

## Purpose
Playwright 기반 End-to-End 테스트입니다. Page Object Model + custom fixture 구조를 따르며, 인증 세션을 한 번만 생성해 재사용합니다. 접근성 검사는 axe-core로 수행합니다.

## Subdirectories
| Directory | Purpose |
|-----------|---------|
| `fixtures/` | `auth.setup.ts`(인증 세션 1회 생성), `test.ts`(커스텀 test/expect export) |
| `page-objects/` | Page Object — `LoginPage`, `PlacesPage`, `PlaceDetailPage`, `OrdersPage` |
| `tests/` | 스펙 — `auth/`(unauthenticated), `places/`·`orders/`·`products/`(authenticated), `accessibility.spec.ts`(axe-core) |
| `.auth/` | 저장된 인증 상태 (gitignore) |

## For AI Agents

### Working In This Directory
- 설정은 루트 `playwright.config.ts` — authenticated/unauthenticated 프로젝트 분리
- 새 테스트는 `fixtures/test.ts`의 커스텀 `test`/`expect`를 import (`@playwright/test` 직접 import 대신)
- UI 셀렉터 로직은 `page-objects/`의 Page Object에 캡슐화 — 스펙에 raw 셀렉터 작성 지양
- 실행: `npm run e2e` (`:ui`, `:debug`, `:headed`, `:report`, `:codegen` 변형)

### Testing Requirements (환경 변수 / GitHub Secrets)
- `E2E_API_URL`, `E2E_USERNAME`, `E2E_PASSWORD`, `E2E_TEST_PLACE_ID`
- CI: `.github/workflows/e2e.yml`

## Dependencies

### External
- `@playwright/test`, `@axe-core/playwright`

<!-- MANUAL: -->
