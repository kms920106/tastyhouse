<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-06-22 | Updated: 2026-06-22 -->

# notice

## Purpose
Notice (공지사항) feature module. Provides the Server Actions, validation schema, and feedback messages backing the `/dashboard/notices` screen. Actions read via `@/api/notice/notice.service` (domain models) and mutate via `@/api/notice/notice.repository` directly, returning typed result objects the UI branches on.

## Key Files
| File | Description |
|------|-------------|
| `actions.ts` | `"use server"` actions: list/detail fetch and create/update/delete. Validates with `noticeFormSchema`, reads via `noticeService` and mutates via `noticeRepository`, and `revalidatePath("/dashboard/notices")` after mutations. Exports `ActionResult` / `NoticeDetailResult` |
| `domain.ts` | App-facing domain models `NoticeListItem` / `NoticeDetail` (flat interfaces, no `extends`). Consumed by `@/api/notice/notice.service` and all notice UI components — this is the only notice type feature/UI code should import |
| `schema.ts` | `noticeFormSchema` (Zod) for the notice form — `title` (≤ `NOTICE_TITLE_MAX` 200), `content` (≤ `NOTICE_CONTENT_MAX` 1000), `visible` (boolean). Exports `NoticeFormValues` |
| `message.ts` | `NOTICE_MESSAGE` — Korean toast/error strings (create/update/delete success, validation and load/delete failure fallbacks) |

## For AI Agents

### Working In This Directory
- `actions.ts` is server-only (`"use server"`); never import it into client components directly — invoke actions through forms/event handlers.
- Reuse `noticeFormSchema` on the client (via `@hookform/resolvers/zod`) and re-validate server-side in the action before persisting.
- Surface outcomes with `NOTICE_MESSAGE` constants; do not hardcode user-facing copy in actions or the UI.
- Keep length limits (`NOTICE_TITLE_MAX`, `NOTICE_CONTENT_MAX`) in sync with the DTO/backend contract in `@/api/notice/`.

### Common Patterns
- Actions return `{ success, message?, id? }` (mutations) or `{ success, data? }` (detail) instead of throwing; the route components render toasts/errors from `success` + `message`.

## Dependencies

### Internal
- `@/api/notice/notice.service` — read access (`getNotices`/`getNotice`), returns domain models (imports `./domain` in turn)
- `@/api/notice/notice.repository` — direct calls for `create`/`update`/`remove` (no transformation needed)
- `./domain` — `NoticeListItem` / `NoticeDetail` domain types used in action signatures and re-used by `notice.service`
- `src/app/(main)/dashboard/notices/_components/` — form/detail/table UI consuming these actions and importing types from `./domain`

### External
- `zod` — validation; `next/cache` — `revalidatePath`

<!-- MANUAL: Any manually added notes below this line are preserved on regeneration -->
