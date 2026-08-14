<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-06-16 | Updated: 2026-07-25 -->

# dashboard

## Purpose
The authenticated dashboard area for 점주(shop owners). `layout.tsx` provides the shared chrome (sidebar, header, content frame) wrapped around every dashboard feature route. Each feature is its own route folder with a `page.tsx` (Server Component) and a colocated `_components/` folder of route-local Client Components.

## Key Files
| File | Description |
|------|-------------|
| `layout.tsx` | Dashboard shell — renders the sidebar (`_components/sidebar`), header, and preference-driven layout; wraps all child routes |
| `page.tsx` | Default dashboard landing (`/dashboard`) |

## Subdirectories

### Shared
| Directory | Purpose |
|-----------|---------|
| `_components/` | Shared dashboard chrome — primarily the `sidebar/` (see `_components/sidebar/AGENTS.md`) |

### Feature routes (each: `page.tsx` + `_components/`, plus `loading.tsx`/`error.tsx`)
| Directory | Purpose |
|-----------|---------|
| `shop/` | 가게 관리 — tabbed 기본정보/운영정보 over `src/api/shop/` via `src/feature/shop/` actions. `page.tsx` reads `searchParams { shopId?, tab? }`, loads 내 가게 목록, falls back to the first shop when `shopId` is absent or not owned, then fetches both tabs' merged views in parallel. `_components/` holds the `Tabs` shell (`shop-manage.tsx`), the shop `Select` (`shop-selector.tsx`, rendered only for 2+ shops), the shared `setting-row.tsx`, one Sheet per setting item, plus `time-select.tsx` (5-minute 시/분 Select pair), `use-image-file-select.ts` (client-side 규격 pre-check via `createImageBitmap`; returns the raw `File` for multipart submission — there is no pre-upload step) and `shop-image-preview.tsx` (renders the server-provided absolute image URL directly, with a mandatory error fallback). 대표이미지/상표 are an approval workflow: the sheet submits a change *request* and displays PENDING/REJECTED status |
| `account/` | 계정 단위 이력 조회 2종 — `login-history/`(개인정보 접속기록, 최근 90일) · `shop-access-history/`(시스템 접근권한 이력, 최근 5년). `shopId` 가 없고 필터·페이지는 전부 URL searchParams 다. `src/api/ceo-login-history/`·`src/api/ceo-shop-access-history/` 를 Server Component 에서 직접 await 하며, 조회 전용이라 Server Action·`revalidatePath` 가 없다. 목록은 `Accordion`(`change-history`/`requests` 선례), 필터 옵션 카탈로그는 서버 엔드포인트가 없어 `src/feature/ceo/constants.ts` 상수다 — 다만 **목록 항목의 한글 라벨은 서버가 준 `resultName`/`actionTypeName` 을 쓰고 프론트에서 매핑하지 않는다**. 접근권한 이력의 가게 Select 는 `getMyShops()` 결과라 **이미 해제된 가게가 빠지므로** 그 한계를 Select 하단 도움말로 안내한다 |
| `shop-status/` | 전체현황·임시중지 — per-shop rows with a 운영상태 Badge, multi-select checkboxes, an 임시중지 `Switch`, a bulk [전체 영업임시중지] button, the suspension Sheet (사유 → 주문유형 → 시작/종료 일시), and a resume `AlertDialog`. `page.tsx` has no summary endpoint to call: it loads 내 가게 목록 then `Promise.all`s `getSuspensions(shopId)` per shop, treating a shop as suspended when **any** record has `releasedAt === null`. An empty `orderMethods` array means "all order methods". The Switch deliberately has **no optimistic update**: suspension blocks orders, so it goes through `useTransition` + server revalidation only |

## For AI Agents

### Working In This Directory
- **Adding a dashboard screen**: create `dashboard/<name>/page.tsx`, colocate UI in `dashboard/<name>/_components/`, add `loading.tsx`/`error.tsx` (copy the `shop/` pair), then register it in `src/navigation/sidebar/sidebar-items.ts` so it appears in the sidebar.
- **사이드바 등록 여부는 화면의 단위로 갈린다.** 위 등록 규칙이 기본값이고, 예외는 *가게 단위* 화면뿐이다.
  - **계정 단위 화면 → 등록한다.** 진입할 부모 화면이 없어 사이드바가 유일한 진입점이다(`account/login-history`·`account/shop-access-history` 가 `내 계정` 그룹으로 등록된 이유).
  - **가게 단위 화면 → 등록하지 않는다.** `?shopId=` 를 요구하므로 가게를 고른 화면(가게 관리 탭)을 거쳐 진입해야 한다 — `shop/change-history`·`shop/requests`·`shop/delivery-area` 가 그렇고, 근거는 `shop/AGENTS.md` 에 있다.
  - 판단 기준은 "이 화면이 `shopId` 없이도 성립하는가?"다. 성립하면 계정 단위이므로 등록한다.
- Shared chrome belongs in `_components/sidebar/`; per-feature widgets stay in that feature's `_components/`. Promote a component to `src/components/` only once a second route uses it.
- `page.tsx` is a Server Component: parse `searchParams` with the `src/lib/utils.ts` helpers (`parseNonNegativeInt`, `parseSearchString`, `parseOptionalBoolean`), call the service/repository, and `throw new Error(<MESSAGE constant>)` on failure so `error.tsx` renders. Client interactivity lives in `_components/` behind `"use client"`.
- Data tables use `@tanstack/react-table` with `manualPagination` driven by URL search params (`router.push` with `URLSearchParams`), not client-side pagination state.
- Radix `Select`'s `value` must stay a stable string for the component's lifetime (`field.value ?? ""`), never flipping to `undefined`.

### Common Patterns
- One route folder per feature; `page.tsx` composes section components from `_components/`.
- **설정 항목 = `setting-row` + Sheet 편집**: on a settings-style screen, each item renders as a `setting-row` (label · current-value summary · [변경] button) and all editing happens in a Sheet opened from that row. The parent tab component owns a single `openSheet` state keyed by item rather than one boolean per sheet. Introduced by `shop/`; reuse it for any future settings screen instead of inlining forms into the list.
- Forms use `react-hook-form` + `zodResolver` + `Controller` around shadcn `Field`/`FieldError` primitives, submit inside `useTransition`, and report outcomes with `toast` using the feature's `message.ts` constants.

## Dependencies

### External
- `@tanstack/react-table`, `react-hook-form`, `@hookform/resolvers/zod`, `zod`, `sonner`, `date-fns`, `lucide-react`

### Internal
- `src/navigation/sidebar/sidebar-items.ts`, `src/components/ui/*`, `src/components/date-range-picker.tsx`, `src/stores/preferences/`, `src/api/`, `src/feature/`, `src/lib/utils.ts`, `src/lib/date.ts`

<!-- MANUAL: Any manually added notes below this line are preserved on regeneration -->
