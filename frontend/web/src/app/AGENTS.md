<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-06-05 | Updated: 2026-06-05 -->

# app

## Purpose
Next.js 15 App Router 라우팅 계층입니다. `page.tsx`는 params/searchParams를 파싱해 `_components/`의 루트 컴포넌트에 위임하는 얇은 진입점이며, 실제 UI·페칭은 `_components/`가 담당합니다. Server Component는 repository를 직접 호출하고, Client Component는 domain hook으로 TanStack Query를 사용합니다.

> **📖 작성 표준은 [`CLAUDE.md`](./CLAUDE.md)에 상세히 정의되어 있습니다. 페이지를 만들거나 수정하기 전에 반드시 읽으세요.** 본 AGENTS.md는 네비게이션 요약입니다.

## Key Files
| File | Description |
|------|-------------|
| `CLAUDE.md` | **App Router 작성 표준 가이드** (필독) |
| `layout.tsx` | Root Layout — QueryProvider, 폰트, AppToaster |
| `error.tsx` | 전역 에러 바운더리 (`'use client'`) |

## Subdirectories
라우트 세그먼트별로 디렉토리가 구성됩니다. 각 라우트는 `page.tsx` + `_components/`(전용 컴포넌트) + 선택적 `_lib/`(전용 유틸)로 구성됩니다.

| Directory | Purpose |
|-----------|---------|
| `(with-footer)/` | 푸터·사이드바 공유 레이아웃 그룹 (홈, places, rank, reviews, mypage) |
| `api/` | Route Handler (`route.ts`) — files, logs, payments |
| `auth/` | 로그인·회원가입·소셜 콜백·비밀번호 찾기 |
| `account/` | 계정 정보·비밀번호·프로필·탈퇴 |
| `places/` | 매장 상세·주문 플로우·예약·상품 (**Best Practice 레퍼런스: `places/[id]/(detail)/`**) |
| `orders/` | 주문 상세·완료·리뷰 작성 |
| `reservations/` | 예약 상세 |
| `products/` | 상품 상세·오늘의 특가 |
| `reviews/` | 리뷰 상세·작성 |
| `members/` | 회원 상세·팔로우·검색 |
| 기타 | `events`, `faqs`, `notices`, `coupons`, `point`, `grade`, `map`, `search`, `setting`, `terms`, `privacy`, `customer-center`, `bug-reports`, `advertising` |

## For AI Agents

### Working In This Directory
- `params` / `searchParams`는 **반드시 `Promise<...>`로 선언하고 `await`** (Next.js 15 필수)
- `page.tsx`에 JSX 로직 작성 금지 → `_components/XxxPage`로 위임
- 비동기 Server Component는 `<Suspense fallback={<...Skeleton />}>`으로 감싸기
- `@tanstack/react-query` 직접 import 금지 → domain hook 사용
- DTO 타입(`*Response`/`*Request`/`*Query`)을 컴포넌트 Props에 직접 사용 금지 → model 타입 사용

### Common Patterns (파일 suffix)
- `Server` = repository 직접 호출 / `Fetcher` = domain hook + 로딩·에러 / `Client` = 인터랙션 / `Section` = Suspense 조합 / `Skeleton` = fallback / `Page` = 섹션 조합 루트

## Dependencies

### Internal
- `@/domains/[domain]/[domain].repository` (Server Component)
- `@/domains/[domain]/[domain].hook` (Client Component)
- `@/actions/[domain]` (hook의 queryFn)
- `@/components/`, `@/lib/paths` (`PAGE_PATHS`)

<!-- MANUAL: -->
