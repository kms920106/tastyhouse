<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-06-05 | Updated: 2026-06-05 -->

# components

## Purpose
여러 라우트에서 공용으로 쓰이는 UI 컴포넌트입니다. 특정 라우트 전용 컴포넌트는 여기가 아니라 해당 라우트의 `_components/`에 둡니다. 도메인 데이터를 표현하는 공용 위젯(매장 카드, 리뷰 패널, 회원 프로필 등)과 범용 UI 프리미티브(`ui/`)로 구성됩니다.

## Subdirectories
| Directory | Purpose |
|-----------|---------|
| `ui/` | 범용 UI 프리미티브 + `App*` 래퍼 + shadcn (see `ui/AGENTS.md`) |
| `layouts/` | Header, Footer, 검색 헤더, header-parts, menu-sidebar |
| `shops/` | 매장 카드·이미지·시설(Facility)·북마크 리스트 위젯 |
| `reviews/` | 리뷰 리스트·패널·평점·이미지 갤러리·옵션 드로어 |
| `products/` | 상품 아이템·카테고리 그룹·리뷰 통계·특가 |
| `members/` | 회원 등급 배지·닉네임·프로필 카드/셀/통계 |
| `orders/` | 장바구니·주문 상품 아이템 |
| `modals/` | 공용 모달 (RankInfo, ShopOwnerMessage) |
| `places/` | (현재 비어 있음) |

## For AI Agents

### Working In This Directory
- **공용성 판단**: 2개 이상 라우트에서 쓰이면 여기, 단일 라우트 전용이면 `src/app/.../_components/`에 둡니다.
- Client 컴포넌트는 첫 줄 `'use client'`
- 데이터 페칭이 필요하면 `@tanstack/react-query` 직접 사용 금지 → domain hook 경유
- Props에 DTO 타입(`*Response` 등) 직접 사용 금지 → model 타입 사용
- Skeleton은 `[Component]Skeleton.tsx`로 동일 디렉토리에 배치

### Common Patterns
- 도메인 코드 → 한국어 변환은 컴포넌트가 아닌 domain `constants`/`service`에서 처리

## Dependencies

### Internal
- `@/components/ui/*` (프리미티브), `@/domains/*` (model 타입·constants), `@/lib/*`

### External
- `radix-ui`, `lucide-react`/`react-icons`, `swiper`, `tailwind-merge`

<!-- MANUAL: -->
