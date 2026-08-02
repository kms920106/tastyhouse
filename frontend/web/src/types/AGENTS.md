<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-06-05 | Updated: 2026-06-05 -->

# types

## Purpose
도메인에 종속되지 않는 전역 타입 정의입니다.

## Key Files
| File | Description |
|------|-------------|
| `common.ts` | `PaginationParams` (`{ page, size }`) 등 공용 타입. dto가 `extends PaginationParams`로 사용 |
| `facebook.d.ts` | Facebook SDK 전역 타입 선언 (`.d.ts`) |

## For AI Agents

### Working In This Directory
- 도메인 전용 타입은 여기가 아니라 `src/domains/[domain]/[domain].types.ts`(Union) 또는 `[domain].model.ts`(엔티티)에 둡니다
- `PaginationParams`는 dto에서 `extends`로 확장하되, 추가 필드가 없으면 빈 인터페이스 대신 직접 사용 (`domains/CLAUDE.md` §3.5)
- server action 파라미터 타입으로 `PaginationParams`를 노출하지 않습니다 (`actions/CLAUDE.md` §8.8)

## Dependencies

### Internal
- `common.ts`는 도메인 dto·repository가 광범위하게 import

<!-- MANUAL: -->
