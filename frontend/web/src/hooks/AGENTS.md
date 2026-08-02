<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-06-05 | Updated: 2026-06-05 -->

# hooks

## Purpose
**도메인과 무관한** 범용 client hook만 둡니다. UI 플로우, 브라우저 유틸리티, 폼 인터랙션 등 여러 도메인에서 재사용되는 hook입니다.

> ⚠️ **도메인 query/mutation hook(`useQuery`/`useMutation`)은 여기가 아니라 `src/domains/[domain]/[domain].hook.ts`에 작성합니다** (`domains/CLAUDE.md` §8.13).

## Key Files
| File | Description |
|------|-------------|
| `use-mobile.ts` | 모바일 뷰포트 감지 |
| `useIntersectionObserver.ts` | 교차 관찰자 (무한스크롤·lazy) |
| `useTabNavigation.ts` | URL searchParam 기반 탭 전환 (`handleTabChange`) |
| `useImageLightbox.ts` | 이미지 라이트박스 상태 |
| `useFileUpload.ts` | 파일 업로드 플로우 |
| `useCartAction.ts` / `useCartInfo.ts` | 장바구니 상태·액션 |
| `useEmailVerification.ts` / `usePhoneAuth.ts` / `usePhoneVerification.ts` | 이메일·휴대폰 인증 플로우 |
| `useProductOptionSelection.ts` | 상품 옵션 선택 |
| `useReviewPanel.ts` | 리뷰 패널 상태 |
| `useTossPayments.ts` | TossPayments SDK 연동 |

## For AI Agents

### Working In This Directory
- 첫 줄 `'use client'`
- **도메인 데이터 페칭 hook은 여기에 만들지 않습니다** → `src/domains/[domain]/[domain].hook.ts`
- `@tanstack/react-query`를 여기서 직접 쓰지 않습니다 (domain hook의 영역)

## Dependencies

### Internal
- `@/lib/*`, `@/domains/*` (constants/model 타입)

### External
- `react`, 브라우저 API

<!-- MANUAL: -->
