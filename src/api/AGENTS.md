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
