<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-06-22 | Updated: 2026-06-22 -->

# feature

## Purpose
Feature modules grouped by business domain. Each module bundles the server-side and validation concerns a feature needs — Server Actions, Zod schemas, and user-facing message constants — sitting between the route UI (`src/app/`) and the data-access layer (`src/api/`). Routes call these actions; the actions call the matching repository in `src/api/<resource>/`.

## Subdirectories
| Directory | Purpose |
|-----------|---------|
| `notice/` | Notice feature: CRUD Server Actions, form schema, and feedback messages (see `notice/AGENTS.md`) |
| `shop/` | Shop feature: gate CRUD plus 9 sub-resource groups (business hours/break times/closed days, amenity/food-type categories + assignments, tags, order methods, banners/photo images, editor choices). First feature consumed by a dynamic detail route (`/dashboard/shops/[id]`). Enum-backed fields (`constants.ts`) are typed against spec examples only — confirm against the backend enum before relying on a value beyond the ones already listed. |

## For AI Agents

### Working In This Directory
- One folder per domain. A typical module exports: `actions.ts` (`"use server"` actions), `schema.ts` (Zod validation + inferred types), and `message.ts` (Korean user-facing toast/error strings).
- Actions import DTOs and repositories from `@/api/<resource>/`; they must not embed raw HTTP/fetch calls — that belongs in the repository.
- After a successful mutation, call `revalidatePath()` for the affected route so server-rendered lists refresh.
- Keep all user-visible copy in `message.ts` (Korean), not inline in actions — consumers reference the constants.

### Common Patterns
- Actions return a small result object (e.g. `{ success, message?, id? }` / `{ success, data? }`) rather than throwing across the server boundary; the UI branches on `success`.
- Validate inputs with the module's Zod schema before touching the repository; on failure return the `INVALID_INPUT` message.

## Dependencies

### Internal
- `src/api/<resource>/` — repositories and DTOs the actions delegate to
- `src/app/(main)/dashboard/<feature>/` — route UI that invokes these actions

### External
- `zod` — schema validation; `next/cache` — `revalidatePath`

<!-- MANUAL: Any manually added notes below this line are preserved on regeneration -->

### Shop 이미지 수정 폼 — 재업로드 필수 정책 (2026-07-26 확인)
가게 관리자 조회 응답이 이미지 fileId 대신 URL만 내려주는 것으로 바뀌면서(`../api/AGENTS.md` 참고), 요청 스펙(fileId 필수)과의 비대칭이 생겼다. 수정 폼(편의시설/음식종류 카테고리, 가게 썸네일)은 기존 이미지를 URL로 미리보기만 하고, 저장하려면 **항상 이미지를 재업로드**하도록 한다 — Zod 스키마의 `imageFileId` required는 그대로 유지해 재업로드를 강제하는 방식. 포토이미지 노출 토글(`PhotoImageUpdateRequest.imageFileId`)처럼 값 하나만 바꾸는 뮤테이션은 재업로드를 요구할 수 없으므로, 백엔드가 해당 요청 필드를 optional로 바꾸기 전까지 UI에서 비활성화한다.
