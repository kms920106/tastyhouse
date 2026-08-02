<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-06-05 | Updated: 2026-06-05 -->

# domains

## Purpose
도메인별 비즈니스 계층입니다. 각 도메인은 7-Layer 패턴(`index` / `types` / `constants` / `model` / `dto` / `repository` / `service` + 조건부 `hook`)으로 책임을 분리합니다. `repository`는 순수 HTTP, `service`는 가공·조합, `hook`은 TanStack Query를 담당합니다.

> **📖 작성 표준은 [`CLAUDE.md`](./CLAUDE.md)에 상세히 정의되어 있습니다. 도메인을 만들거나 수정하기 전에 반드시 읽으세요.** Best Practice 레퍼런스는 `shop` 도메인(7개 파일 전체 구성)입니다.

## Subdirectories
| Directory | Purpose |
|-----------|---------|
| `shop/` | 매장 — **7-Layer 전체 구성 레퍼런스** (constants/dto/hook/model/repository/service/types) |
| `auth/` | 인증 (로그인·소셜·토큰), `auth.model.ts`에 `SocialProfile` 등 |
| `member/` | 회원 정보·등급·프로필 |
| `order/` | 주문·장바구니·주문수단 |
| `reservation/` | 예약 |
| `payment/` | 결제 (TossPayments 연동) |
| `product/` | 상품 |
| `review/` | 리뷰·댓글 |
| `rank/` | 랭킹 |
| `event/` | 이벤트 |
| `follow/` | 팔로우 |
| `policies/` | 약관·개인정보처리방침 본문 |
| 기타 | `banner`, `bug-report`, `email-verification`, `faq`, `file`, `grade`, `notice`, `partnership`, `phone-verification`, `search` |

## For AI Agents

### Working In This Directory
- `repository.ts` / `service.ts` 첫 줄은 `import 'server-only'` — client에서 import 불가
- `index.ts`는 **client-safe layer(`types`, `constants`, `model`, `dto`)만** `export *` — `repository`/`service`/`hook` export 금지
- `hook.ts` 첫 줄은 `'use client'`, `queryFn`은 `@/actions/[domain]` server action만 호출 (repository/service 직접 import 금지)
- `repository`에 가공 로직(map/filter/조합) 금지 → `service`로
- `service`에서 `api.*` 직접 호출 금지 → 반드시 `repository` 경유
- 다른 도메인 참조 시 내부 파일이 아닌 폴더 index(`../[other]`) 경로 사용

### 파일별 책임 (요약 — 상세는 CLAUDE.md)
| 파일 | 책임 |
|------|------|
| `types.ts` | Backend enum ↔ Union 리터럴 (`'KOREAN' \| 'JAPANESE'`) |
| `model.ts` | 재사용 엔티티 인터페이스 (컴포넌트 Props용) |
| `dto.ts` | API Request/Response/Query (`interface`만) |
| `repository.ts` | HTTP 호출만 |
| `service.ts` | 가공·조합·비즈니스 로직 |
| `constants.ts` | 코드 ↔ 한국어 라벨 매핑 |
| `hook.ts` | TanStack Query custom hook + queryKey |

## Dependencies

### Internal
- `@/lib/api` (repository), `@/types/common` (`PaginationParams`), `@/actions/[domain]` (hook)

### External
- `@tanstack/react-query` (hook only)

<!-- MANUAL: -->
