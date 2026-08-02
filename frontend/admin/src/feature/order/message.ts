/** 주문 화면 헤더 정적 문구 (목록/로딩 공용) */
export const ORDER_PAGE_COPY = {
  TITLE: "주문",
  DESCRIPTION: "주문 목록을 조회하고 상태 변경, 삭제를 관리합니다.",
} as const;

/** 주문 사용자 피드백 메시지 (동적/토스트/에러 폴백) */
export const ORDER_MESSAGE = {
  // 성공 toast
  STATUS_UPDATE_SUCCESS: "주문 상태를 변경했습니다.",
  DELETE_SUCCESS: "주문을 삭제했습니다.",

  // 에러 폴백
  STATUS_UPDATE_FAILED: "주문 상태 변경 중 오류가 발생했습니다.",
  DELETE_FAILED: "주문 삭제 중 오류가 발생했습니다.",
  INVALID_INPUT: "입력값이 올바르지 않습니다.",
  LIST_LOAD_FAILED: "주문 목록을 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.",
  DETAIL_LOAD_FAILED: "주문 상세를 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.",
} as const;
