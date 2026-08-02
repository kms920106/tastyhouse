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
| `notice/notice.repository.ts` | Notice resource repository — pure HTTP, returns DTOs (`*Response`), no transformation |
| `notice/notice.dto.ts` | Notice request/response DTOs (`*Response`/`*Request`), internal to this layer |
| `notice/notice.service.ts` | Notice read service — calls repository and maps DTO → domain (`@/feature/notice/domain`) for queries that need it |
| `file/file.repository.ts` | File Admin upload repository — `POST /api/files/v1/upload` (multipart), returns the uploaded fileId (`number`) |
| `file/file.dto.ts` | File upload DTOs — `FileUploadResponse` (= fileId `number`), `ALLOWED_IMAGE_TYPES`, `MAX_IMAGE_SIZE_BYTES` |
| `shop/shop.repository.ts` | Shop Admin repository — largest resource so far: `getStations()` (GET `/v1/stations`, 등록/수정 폼 드롭다운용) plus gate CRUD and 9 sub-resource groups (business hours/break times/closed days, amenity/food-type master categories + assignments, tags, order methods, banners/photo categories/photo images, editor choices). Sub-resource update/delete routes key off the sub-resource's own ID, not the parent shopId — mirrors the `option-groups/{groupId}/options` trap in `product.repository.ts` |
| `shop/shop.dto.ts` | Shop DTOs — enum fields (`dayType`, `amenity`, `foodType`, `orderMethod`, `closedDayType`) are confirmed against the backend enums (2026-07-19); the canonical value/label catalog lives in `src/feature/shop/constants.ts`. List responses carry server-rendered Korean labels — `description` (business hours/break times/closed days) and `displayName` (order methods) — which the UI renders for existing items, while the `*_LABEL` constants drive only the create/edit dropdown option catalog. Photo image create/update bodies require `visible: boolean`. |

## For AI Agents

### Working In This Directory
- Group code by resource: each resource gets its own folder (e.g. `notice/`) holding its repository, DTOs, and service.
- Cross-resource HTTP plumbing and shared types belong in `shared/`.
- Mock modules here are illustrative. When wiring a real backend, route calls through the repository + `shared/client` rather than hardcoding fetches in consumers.
- Notice's domain models (`NoticeListItem`, `NoticeDetail`) live in `@/feature/notice/domain`, not here — `notice.service.ts` imports them to type its return values. This means `api/notice` depends on `feature/notice` for this resource; keep that dependency one-directional (service imports domain types only, never the reverse).

### Common Patterns
- `repository` = transport only, returns DTOs (`*Response`). `service` = read-side transformation, returns domain models — only add a service method when a DTO → domain mapping is actually needed. Mutations with no transformation (`create`/`update`/`remove`) are called directly on the repository from feature actions.
- DTOs never leave this layer; feature/UI code imports domain types from `@/feature/notice/domain`, never from `notice.dto.ts`.
- Plain typed TS modules; mock datasets export arrays/objects.

<!-- MANUAL: Any manually added notes below this line are preserved on regeneration -->

### Shop 이미지 필드 비대칭 (2026-07-26 확인)
`shop.dto.ts`의 이미지 관련 응답(`*Response`)과 요청(`*CreateRequest`/`*UpdateRequest`)은 필드 형태가 다르다. **조회 응답은 바로 접근 가능한 `imageUrl`/`activeImageUrl`/`inactiveImageUrl`/`thumbnailImageUrl`(문자열 URL)을 내려주고, 생성·수정 요청은 여전히 업로드 후 받은 `imageFileId`(숫자)를 요구한다.** 즉 목록/상세 조회에서는 fileId를 전혀 받을 수 없으므로, 수정 폼에서 "기존 이미지 유지"를 하려면 재업로드가 필요하다 — `../feature/AGENTS.md`의 재업로드 정책 참고.
