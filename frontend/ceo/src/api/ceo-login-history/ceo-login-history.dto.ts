/**
 * 개인정보 접속기록(로그인 이력) 조회 DTO — `docs/tasks/backend.md` §2-1 응답과 1:1 대응.
 *
 * 이 레이어를 벗어나지 않는다(`src/api/AGENTS.md`). UI/feature 는
 * `@/feature/ceo/domain` 의 도메인 타입만 import 한다.
 */

/** 목록 조회 query 파라미터. 날짜는 `yyyy-MM-dd`, 생략 시 서버 기본값(오늘 - 29일 ~ 오늘). */
export interface CeoLoginHistoryListQueryRequest {
  // backend.md §2-1 — SUCCESS | FAILURE. 생략 시 전체
  result?: string;
  startDate?: string;
  endDate?: string;
}

export interface CeoLoginHistoryItemResponse {
  id: number;
  // backend.md §2-1 — SUCCESS | FAILURE
  result: string;
  /** 한글 라벨 (`로그인 성공` / `로그인 실패`) */
  resultName: string;
  // backend.md §2-1 — BAD_CREDENTIALS | ACCOUNT_INACTIVE. 성공 시 null
  failureReason: string | null;
  failureReasonName: string | null;
  ipAddress: string | null;
  /** 서버가 500자로 절단해 저장한 User-Agent */
  userAgent: string | null;
  /** ISO-8601 LocalDateTime (예: "2026-08-14T19:46:03") */
  loggedInAt: string;
}
