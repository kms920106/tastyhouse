/**
 * 점주 계정 단위 화면의 필터 옵션 카탈로그.
 *
 * 변경이력·요청처리 현황과 달리 **서버 카탈로그 엔드포인트가 없다**(`docs/tasks/backend.md` §2-1·§2-2 —
 * 두 엔드포인트 모두 목록만 제공한다). 그래서 옵션 목록을 프론트 상수로 둔다.
 *
 * 목록 항목의 라벨은 여기서 매핑하지 않는다 — 서버가 `resultName`/`actionTypeName` 을 함께
 * 내려주므로 렌더는 그 값을 쓰고, 이 상수는 **필터 드롭다운의 선택지 카탈로그 + URL 검증 화이트리스트**
 * 로만 쓰인다. 백엔드에 상수가 추가되면 여기에도 추가한다.
 */

export interface CeoFilterOption {
  code: string;
  name: string;
}

/** 로그인 결과 (`docs/tasks/backend.md` §2-1 `result`) */
export const CEO_LOGIN_RESULT_OPTIONS: readonly CeoFilterOption[] = [
  { code: "SUCCESS", name: "로그인 성공" },
  { code: "FAILURE", name: "로그인 실패" },
] as const;

/** 접근권한 조치 유형 (`docs/tasks/backend.md` §2-2 `actionType`) */
export const CEO_SHOP_ACCESS_ACTION_TYPE_OPTIONS: readonly CeoFilterOption[] = [
  { code: "GRANT", name: "권한 부여" },
  { code: "REVOKE", name: "권한 말소" },
] as const;

/** 개인정보 접속기록 보관 기간 (`docs/tasks/backend.md` §2-1 `RETENTION_DAYS`) */
export const CEO_LOGIN_HISTORY_RETENTION_DAYS = 90;

/** 시스템 접근권한 이력 보관 기간 (`docs/tasks/backend.md` §2-2 `RETENTION_YEARS`) */
export const CEO_SHOP_ACCESS_HISTORY_RETENTION_YEARS = 5;
