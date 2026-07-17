/** 포인트 화면 헤더 정적 문구 (Sheet 제목/설명) */
export const POINT_PAGE_COPY = {
  TITLE: "포인트 관리",
  DESCRIPTION: "회원의 포인트 잔액과 이력을 조회하고 수동 적립, 차감을 관리합니다.",
} as const;

/** 포인트 사용자 피드백 메시지 (동적/토스트/에러 폴백) */
export const POINT_MESSAGE = {
  // 성공 toast
  EARN_SUCCESS: "포인트를 적립했습니다.",
  DEDUCT_SUCCESS: "포인트를 차감했습니다.",

  // 에러 폴백
  EARN_FAILED: "포인트 적립 처리 중 오류가 발생했습니다.",
  DEDUCT_FAILED: "포인트 차감 처리 중 오류가 발생했습니다.",
  INVALID_INPUT: "입력값이 올바르지 않습니다.",
  BALANCE_LOAD_FAILED: "포인트 잔액을 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.",
  HISTORY_LOAD_FAILED: "포인트 이력을 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.",
} as const;
