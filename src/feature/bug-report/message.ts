/** 버그 제보 화면 헤더 정적 문구 (목록/로딩 공용) */
export const BUG_REPORT_PAGE_COPY = {
  TITLE: "버그 제보",
  DESCRIPTION: "회원이 제출한 버그 제보를 열람하고 처리합니다.",
} as const;

/** 버그 제보 사용자 피드백 메시지 (동적/토스트/에러 폴백) */
export const BUG_REPORT_MESSAGE = {
  // 성공 toast
  STATUS_UPDATE_SUCCESS: "처리 상태가 변경되었습니다.",
  CLASSIFY_SUCCESS: "분류/우선순위가 저장되었습니다.",
  ASSIGN_SUCCESS: "담당자가 배정되었습니다.",

  // 에러 폴백
  STATUS_UPDATE_FAILED: "처리 상태 변경 중 오류가 발생했습니다.",
  CLASSIFY_FAILED: "분류/우선순위 저장 중 오류가 발생했습니다.",
  ASSIGN_FAILED: "담당자 배정 중 오류가 발생했습니다.",
  INVALID_INPUT: "입력값이 올바르지 않습니다.",
  LIST_LOAD_FAILED: "버그 제보 목록을 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.",
  DETAIL_LOAD_FAILED: "버그 제보 상세를 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.",
} as const;
