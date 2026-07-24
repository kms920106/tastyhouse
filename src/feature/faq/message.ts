/** FAQ 화면 헤더 정적 문구 (목록/로딩 공용) */
export const FAQ_PAGE_COPY = {
  TITLE: "FAQ",
  DESCRIPTION: "자주 묻는 질문을 등록하고 관리합니다.",
} as const;

/** FAQ 사용자 피드백 메시지 (동적/토스트/에러 폴백) */
export const FAQ_MESSAGE = {
  // 성공 toast
  CREATE_SUCCESS: "FAQ가 등록되었습니다.",
  UPDATE_SUCCESS: "FAQ가 수정되었습니다.",
  DELETE_SUCCESS: "FAQ가 삭제되었습니다.",

  // 에러 폴백
  CREATE_UPDATE_FAILED: "처리 중 오류가 발생했습니다.",
  DELETE_FAILED: "삭제 중 오류가 발생했습니다.",
  INVALID_INPUT: "입력값이 올바르지 않습니다.",
  LIST_LOAD_FAILED: "FAQ 목록을 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.",
  DETAIL_LOAD_FAILED: "FAQ 상세를 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.",
} as const;

/** FAQ 카테고리 관리 시트 정적 문구 */
export const FAQ_CATEGORY_PAGE_COPY = {
  TITLE: "카테고리 관리",
  DESCRIPTION: "FAQ 카테고리를 등록하고 관리합니다.",
} as const;

/** FAQ 카테고리 사용자 피드백 메시지 */
export const FAQ_CATEGORY_MESSAGE = {
  // 성공 toast
  CREATE_SUCCESS: "카테고리가 등록되었습니다.",
  UPDATE_SUCCESS: "카테고리가 수정되었습니다.",
  DELETE_SUCCESS: "카테고리가 삭제되었습니다.",

  // 에러 폴백
  CREATE_UPDATE_FAILED: "처리 중 오류가 발생했습니다.",
  DELETE_FAILED: "삭제 중 오류가 발생했습니다.",
  INVALID_INPUT: "입력값이 올바르지 않습니다.",
  LIST_LOAD_FAILED: "카테고리 목록을 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.",
  DETAIL_LOAD_FAILED: "카테고리 상세를 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.",
} as const;
