<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-06-16 | Updated: 2026-06-22 -->

# api

## Purpose
Data-access layer for the app. Repositories, DTOs, and shared HTTP plumbing live here so that UI and Server Actions depend on a stable API surface rather than on transport details. Some in-repo datasets (e.g. mock users) still live here until backed by a real endpoint.

## Key Files
| File | Description |
|------|-------------|
| `users.ts` | Mock user records used by the Users management screen and other user-facing UI |
| `shared/client.ts` | Shared HTTP client used by repositories |
| `shared/types.ts` | Shared API types (`ApiResponse`, `ApiPagination`, etc.) |
| `file/file.repository.ts` | File Admin upload repository — `POST /api/files/v1/upload` (multipart), returns the uploaded fileId (`number`) |
| `file/file.dto.ts` | File upload DTOs — `FileUploadResponse` (= fileId `number`), `ALLOWED_IMAGE_TYPES`, `MAX_IMAGE_SIZE_BYTES` |
| `shop/shop.repository.ts` | Shop repository for the **점주(shop owner)** scope — largest resource here. Wraps 내 가게 목록/상세, 편의시설 카테고리 마스터, and the sub-resource groups: business hours / break times / closed-days (공휴일·정기·임시 통합) / hygiene-badges (운영정보), thumbnail / trademark (승인 워크플로) / introduction (+validate) / content-boards / phone-numbers / status / convenience-info / amenities (기본정보), suspensions (전체현황·임시중지). **Path conventions differ per sub-resource — do not generalize.** business-hours/break-times/closed-days/phone-numbers update+delete key off the sub-resource's own ID (`PUT /v1/business-hours/{id}`); content-boards and suspensions stay under the parent `{shopId}` (`DELETE /v1/{shopId}/content-boards/{id}`); amenities delete keys off `amenityCategoryId`. Every such method carries an inline warning comment — follow `docs/CEO-API-SHOP-SPEC-FOR-FRONTEND.md` exactly rather than inferring. `POST /v1/suspensions/bulk` is not under `{shopId}` because it applies to many shops at once |
| `shop/shop.dto.ts` | Shop DTOs, sourced entirely from `docs/CEO-API-SHOP-SPEC-FOR-FRONTEND.md`. The canonical value/label catalog lives in `src/feature/shop/constants.ts`. List responses carry server-rendered Korean labels — `description` (business/break times, closed days) and `displayName` (amenities) — which the UI renders for existing items, while the `*_LABEL` constants drive only the create/edit dropdown option catalog. `BusinessHourResponse.is24Hours` marks 24시간 영업, in which case `openTime`/`closeTime` are ignored. Images are referenced by **fileId only** (`thumbnailImageFileId`, `trademarkImageFileId`, `imageFileId`) — the spec provides no display URL; see `src/feature/shop/image.ts` |
| `shop/shop.service.ts` | Shop read service — the only place DTO → domain mapping happens for shop. Exposes `getMyShops`, `getShopBasicInfo(shopId)`, `getShopOperationInfo(shopId)`; the latter two fan out with `Promise.all` and, if any sub-call returns an `error`, propagate that `error` unchanged so the route falls through to `error.tsx`. There is no 전체현황 summary endpoint in the spec — `/dashboard/shop-status` aggregates per-shop `getSuspensions` in its own `page.tsx` |

## For AI Agents

### Working In This Directory
- Group code by resource: each resource gets its own folder (e.g. `shop/`) holding its repository, DTOs, and service.
- Cross-resource HTTP plumbing and shared types belong in `shared/`.
- Domain models live in `@/feature/<domain>/domain`, not here — a resource's `*.service.ts` imports them to type its return values (e.g. `api/shop` depends on `feature/shop/domain`). Keep that dependency one-directional (service imports domain types only, never the reverse).
- Mock modules here are illustrative. When wiring a real backend, route calls through the repository + `shared/client` rather than hardcoding fetches in consumers.

### Common Patterns
- `repository` = transport only, returns DTOs (`*Response`). `service` = read-side transformation, returns domain models — only add a service method when a DTO → domain mapping is actually needed. Mutations with no transformation (`create`/`update`/`remove`) are called directly on the repository from feature actions.
- **하위 리소스 경로 규칙은 리소스마다 다르다 — 절대 규칙으로 일반화하지 말 것.** 신규 리소스를 추가할 때 "하위 리소스 수정/삭제는 하위 리소스 ID 경로"(`/business-hours/{id}`)를 기본값으로 삼는 것은 합리적이지만, `shop`의 content-boards·suspensions 처럼 스펙이 부모 `{shopId}` 경로를 유지하라고 명시하면 그 예외를 따른다. 새 리소스를 추가할 때는 항상 실제 API 스펙 문서를 먼저 확인하고, 규칙에서 벗어나는 경로마다 그 이유를 인라인 주석으로 남긴다(`shop/shop.repository.ts` 참고).
- DTOs never leave this layer; feature/UI code imports domain types from `@/feature/<domain>/domain` (e.g. `@/feature/shop/domain`), never from `*.dto.ts`.
- Plain typed TS modules; mock datasets export arrays/objects.

<!-- MANUAL: Any manually added notes below this line are preserved on regeneration -->
