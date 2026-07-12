/** 배너 화면 헤더 정적 문구 (목록/로딩 공용) */
export const BANNER_PAGE_COPY = {
  TITLE: "배너",
  DESCRIPTION: "홈/사이드바에 노출되는 배너를 등록하고 관리합니다.",
} as const;

/** 배너 사용자 피드백 메시지 (동적/토스트/에러 폴백) */
export const BANNER_MESSAGE = {
  // 성공 toast
  CREATE_SUCCESS: "배너가 등록되었습니다.",
  UPDATE_SUCCESS: "배너가 수정되었습니다.",
  DELETE_SUCCESS: "배너가 삭제되었습니다.",

  // 에러 폴백
  CREATE_UPDATE_FAILED: "처리 중 오류가 발생했습니다.",
  DELETE_FAILED: "삭제 중 오류가 발생했습니다.",
  INVALID_INPUT: "입력값이 올바르지 않습니다.",
  LIST_LOAD_FAILED: "배너 목록을 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.",
  DETAIL_LOAD_FAILED: "배너 상세를 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.",
  IMAGE_UPLOAD_FAILED: "이미지 업로드 중 오류가 발생했습니다.",
  IMAGE_TYPE_INVALID: "jpg, png, gif, webp 형식의 이미지만 업로드할 수 있습니다.",
  IMAGE_SIZE_EXCEEDED: "이미지 크기는 최대 10MB까지 업로드할 수 있습니다.",
} as const;

export const BANNER_TYPE_LABEL = {
  HOME: "홈",
  SIDEBAR: "사이드바",
} as const;
