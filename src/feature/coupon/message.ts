/** 쿠폰 화면 헤더 정적 문구 (목록/로딩 공용) */
export const COUPON_PAGE_COPY = {
  TITLE: "쿠폰",
  DESCRIPTION: "할인 쿠폰을 등록하고 회원에게 발급합니다.",
} as const;

/** 쿠폰 사용자 피드백 메시지 (동적/토스트/에러 폴백) */
export const COUPON_MESSAGE = {
  // 성공 toast
  CREATE_SUCCESS: "쿠폰이 등록되었습니다.",
  UPDATE_SUCCESS: "쿠폰이 수정되었습니다.",
  DELETE_SUCCESS: "쿠폰이 삭제되었습니다.",
  ISSUE_SUCCESS: "쿠폰이 발급되었습니다.",

  // 에러 폴백
  CREATE_UPDATE_FAILED: "처리 중 오류가 발생했습니다.",
  DELETE_FAILED: "삭제 중 오류가 발생했습니다.",
  ISSUE_FAILED: "발급 중 오류가 발생했습니다.",
  INVALID_INPUT: "입력값이 올바르지 않습니다.",
  LIST_LOAD_FAILED: "쿠폰 목록을 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.",
  DETAIL_LOAD_FAILED: "쿠폰 상세를 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.",
  ISSUES_LOAD_FAILED: "발급 현황을 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.",
} as const;
