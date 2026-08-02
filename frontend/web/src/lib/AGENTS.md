<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-06-05 | Updated: 2026-06-05 -->

# lib

## Purpose
도메인에 종속되지 않는 순수 유틸리티와 인프라 모듈입니다. API 클라이언트, 인증 설정, 경로 상수, 포맷팅·계산 헬퍼, 로깅을 제공합니다.

## Key Files
| File | Description |
|------|-------------|
| `api.ts` | **API 클라이언트** — `ApiClient` 클래스, `ApiResponse<T>` 타입 (`{ data?, error?, status, pagination? }`). repository가 이 `api`를 사용 |
| `paths.ts` | `PAGE_PATHS` — 모든 페이지 경로 상수 (중앙 관리) |
| `auth-config.ts` | `AUTH_COOKIE_KEYS`, `getTokenMaxAge` — 인증 쿠키 키·만료 |
| `env.ts` | 환경 변수 타입 안전 접근 (`env.NEXT_PUBLIC_API_URL` 등) |
| `utils.ts` | `cn` (className 병합) 등 범용 헬퍼 |
| `logger.ts` / `logger-browser.ts` | pino 서버/브라우저 로거 |
| `date.ts` / `number.ts` | 날짜·숫자 포맷팅 |
| `cart.ts` / `order.ts` / `paymentCalculation.ts` | 장바구니·주문·결제 금액 계산 |
| `review.ts` | 리뷰 관련 순수 헬퍼 |
| `form.ts` / `sanitize.ts` | 폼 유틸·HTML sanitize |
| `uploadFile.ts` / `share.ts` / `delay.ts` | 파일 업로드·공유·지연 유틸 |

## For AI Agents

### Working In This Directory
- **순수 함수·인프라만** — React 컴포넌트, 도메인 비즈니스 로직 금지
- `api.ts`는 `cookies()`(next/headers)를 사용하므로 주로 server 컨텍스트(repository)에서 호출됩니다
- 새 페이지 경로는 `paths.ts`의 `PAGE_PATHS`에 추가하고 문자열 하드코딩을 피합니다
- 환경 변수는 `process.env` 직접 접근 대신 `env.ts` 경유

## Dependencies

### Internal
- `auth-config.ts`, `env.ts`는 `api.ts`·`middleware.ts`가 공유
- `paths.ts` → `@/domains/order` (`OrderMethodType`)

### External
- `next/headers` (cookies), `pino`/`pino-pretty`, `clsx`, `tailwind-merge`

<!-- MANUAL: -->
