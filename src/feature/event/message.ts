/** 이벤트 화면 헤더 정적 문구 (목록/로딩 공용) */
export const EVENT_PAGE_COPY = {
  TITLE: "이벤트",
  DESCRIPTION: "이벤트를 등록하고 당첨자 발표 공지와 당첨자를 관리합니다.",
} as const;

/** 이벤트 사용자 피드백 메시지 (동적/토스트/에러 폴백) */
export const EVENT_MESSAGE = {
  // 성공 toast
  CREATE_SUCCESS: "이벤트가 등록되었습니다.",
  UPDATE_SUCCESS: "이벤트가 수정되었습니다.",
  DELETE_SUCCESS: "이벤트가 삭제되었습니다.",
  ANNOUNCEMENT_CREATE_SUCCESS: "당첨자 발표 공지가 등록되었습니다.",
  ANNOUNCEMENT_UPDATE_SUCCESS: "당첨자 발표 공지가 수정되었습니다.",
  WINNER_CREATE_SUCCESS: "당첨자가 등록되었습니다.",
  WINNER_DELETE_SUCCESS: "당첨자가 삭제되었습니다.",

  // 에러 폴백
  CREATE_UPDATE_FAILED: "처리 중 오류가 발생했습니다.",
  DELETE_FAILED: "삭제 중 오류가 발생했습니다.",
  ANNOUNCEMENT_SAVE_FAILED: "공지 저장 중 오류가 발생했습니다.",
  WINNER_SAVE_FAILED: "당첨자 등록 중 오류가 발생했습니다.",
  WINNER_DELETE_FAILED: "당첨자 삭제 중 오류가 발생했습니다.",
  INVALID_INPUT: "입력값이 올바르지 않습니다.",
  LIST_LOAD_FAILED: "이벤트 목록을 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.",
  DETAIL_LOAD_FAILED: "이벤트 상세를 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.",
  ANNOUNCEMENT_LOAD_FAILED: "공지를 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.",
  WINNERS_LOAD_FAILED: "당첨자 목록을 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.",

  // 이미지 업로드
  IMAGE_UPLOAD_FAILED: "이미지 업로드에 실패했습니다.",
  IMAGE_TYPE_INVALID: "jpg, png, gif, webp 형식의 이미지만 업로드할 수 있습니다.",
  IMAGE_SIZE_EXCEEDED: "이미지 크기는 최대 10MB까지 업로드할 수 있습니다.",
} as const;
