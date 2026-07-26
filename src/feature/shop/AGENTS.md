<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-07-25 | Updated: 2026-07-25 -->

# shop

## Purpose
Shop (가게 관리) feature module for the **점주(shop owner)** scope. Provides the Server Actions, validation schemas, enum catalog, time math, and feedback messages backing the `/dashboard/shop` (기본정보/운영정보 tabs) and `/dashboard/shop-status` (전체현황·임시중지) screens. Actions read merged views via `@/api/shop/shop.service` and mutate via `@/api/shop/shop.repository` directly, returning typed result objects the UI branches on.

> The backend contract is still being built. The assumed contract lives entirely in `@/api/shop/shop.dto.ts` + `shop.repository.ts` — this module depends only on `./domain`, so a spec change should not reach here.

## Key Files
| File | Description |
|------|-------------|
| `actions.ts` | `"use server"` actions, one per setting item: `update*Action` / `create*Action` / `delete*Action`, plus `fetch*Action` for sheet-local refetches. Image and 콘텐츠보드 mutations take a `FormData` directly (multipart passthrough to the repository) and re-validate MIME/size server-side via `extractFile`; there is no separate pre-upload step. Mutations `revalidatePath("/dashboard/shop")`, suspension actions `revalidatePath("/dashboard/shop-status")` |
| `domain.ts` | App-facing domain models (`ShopSummary`, `ShopBasicInfo`, `ShopOperationInfo`, `BusinessHour`, `BreakTime`, `RegularClosedDay`, `TemporaryClosure`, `ShopClosedDays`, `HygieneBadge`, `PhoneNumber`, `ContentBoardItem`, `ImageChangeRequest`, `ShopImageStatus`, `ShopConvenienceInfo`, `ShopAmenity`, `AmenityCategory`, `Suspension`). Re-exports the enum string unions from the DTO layer so UI never imports `shop.dto.ts`. The only shop types feature/UI code should import |
| `constants.ts` | Enum catalog as `*_OPTIONS` (`as const` array) + `*_LABEL` (`Record`) pairs, plus input limits and `SHOP_MANAGE_TABS`. All values are now sourced from `docs/CEO-API-SHOP-SPEC-FOR-FRONTEND.md`: `DAY_TYPE_*`, `CLOSED_DAY_TYPE_*` (43종), `ORDER_METHOD_*`, `SHOP_STATUS_*` (OPEN/HIDDEN only), `APPROVAL_STATUS_*`, `CONTENT_BOARD_TYPE/TOPIC_*`, `SUSPENSION_REASON_*`, `HYGIENE_BADGE_TYPE_*`. 편의시설(amenities) has no hardcoded enum — the catalog is fetched at runtime via `fetchAmenityCategoriesAction` |
| `schema.ts` | Zod schemas per setting item. Cross-field rules use `.superRefine()`: 콘텐츠보드 VIDEO ⇒ YouTube URL only (no file), 영업시간 1h~23h55m, 휴게시간 must sit inside 영업시간, 휴게시간 ≠ 영업시간, 임시휴무 ≤ 30일, 임시중지 endAt > startAt. 5-minute granularity via a shared `timeString` refine. 대표번호 is assigned server-side (first number wins), so there is no client-side "exactly one primary" rule |
| `time.ts` | Pure time helpers, deliberately UI-free so the overnight/clamp logic is testable: `parseTimeToMinutes`, `formatMinutesToTime`, `formatTimeLabel`, `isTimeStepValid`, `getDurationMinutes`, `isRangeWithin`, `isSameRange`, `clampRangeToBusinessHours`, `HOUR_OPTIONS`/`MINUTE_OPTIONS`, `countInclusiveDays`. Overnight ranges are normalized by adding 24h when end ≤ start |
| `message.ts` | Korean copy only: `SHOP_PAGE_COPY` / `SHOP_STATUS_PAGE_COPY` (page+loading headers), `SHOP_BASIC_COPY` / `SHOP_OPERATION_COPY` (setting-row labels and guidance), `SHOP_MESSAGE` (toasts, error fallbacks, validation notices) |

## For AI Agents

### Working In This Directory
- `actions.ts` is server-only (`"use server"`); invoke actions from client components through event handlers, never import it into server-rendered module scope for side effects.
- Reuse the schemas on the client via `@hookform/resolvers/zod` and re-validate server-side in the action before persisting.
- Surface outcomes with `SHOP_MESSAGE` / `*_COPY` constants; never hardcode user-facing copy in actions or components.
- Enum labels: prefer the server-provided Korean label (`description` on business hours/break times/closed days, `displayName` on amenities) when rendering an existing item. `*_LABEL` maps are for rendering the dropdown option catalog only — the backend enums may drift from the 2026-07-19 snapshot.
- Keep overnight/range math in `time.ts` as pure functions. Do not inline minute arithmetic into components.

### Common Patterns
- Actions return `{ success, message?, id? }` (mutations) or `{ success, message?, data? }` (queries) instead of throwing; the UI branches on `success`.
- Validation failure returns the first Zod issue's message, falling back to `SHOP_MESSAGE.INVALID_INPUT`.
- Image규격 (상표 900KB/JPG/560²/1:1, 대표이미지 700², 콘텐츠보드 700²·GIF 250²) is pre-checked client-side with `createImageBitmap` (see `dashboard/shop/_components/use-image-file-select.ts`) and re-checked for MIME/size by `extractFile` inside the action.
- 대표이미지/상표 changes are an approval workflow, not an instant update: `requestThumbnailChangeAction` / `requestTrademarkChangeAction` submit a request, and the UI reflects `thumbnailStatus`/`trademarkStatus` (PENDING badge, REJECTED `rejectReason`).
- 이미지 URL은 API가 바로 표시 가능한 절대 URL(`thumbnailImageUrl`/`trademarkImageUrl`/`currentImageUrl`/`imageUrl`)로 내려주므로 별도 변환 없이 그대로 쓴다. 존재하지 않을 수 있으니 `onError` 폴백은 계속 유지한다.

## Dependencies

### Internal
- `@/api/shop/shop.service` — merged read paths (`getMyShops`, `getShopBasicInfo`, `getShopOperationInfo`). There is no 전체현황 summary endpoint in the spec; `/dashboard/shop-status` aggregates per-shop `getSuspensions` itself
- `@/api/shop/shop.repository` — direct calls for every mutation and for single-resource refetches
- `@/api/file/file.dto` — `ALLOWED_IMAGE_TYPES` / `MAX_IMAGE_SIZE_BYTES` for server-side re-validation
- `./domain` — domain types used in action signatures and re-used by `shop.service`
- `src/app/(main)/dashboard/shop/_components/`, `src/app/(main)/dashboard/shop-status/_components/` — route UI consuming these actions

### External
- `zod` — validation; `next/cache` — `revalidatePath`

<!-- MANUAL: Any manually added notes below this line are preserved on regeneration -->
