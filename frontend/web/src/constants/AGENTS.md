<!-- Parent: ../AGENTS.md -->
<!-- Generated: 2026-06-05 | Updated: 2026-06-05 -->

# constants

## Purpose
도메인에 종속되지 않는 전역 상수입니다. 공용 에러 메시지와 입력 검증 규칙을 정의합니다.

## Key Files
| File | Description |
|------|-------------|
| `errors.ts` | `COMMON_ERROR_MESSAGES` — 페칭/렌더/뮤테이션 공용 에러 문구. `FETCH_ERROR(subject)`는 함수형 메시지 |
| `validation.ts` | 입력 검증 규칙(정규식·길이 제한 등) |

## For AI Agents

### Working In This Directory
- 도메인 코드 ↔ 한국어 라벨 매핑은 여기가 아니라 `src/domains/[domain]/[domain].constants.ts`에 둡니다
- 에러 표시 컴포넌트(`FetchErrorState` 등)는 `COMMON_ERROR_MESSAGES`를 사용합니다

## Dependencies

### Internal
- 여러 계층이 import (app `_components`, domains)

<!-- MANUAL: -->
