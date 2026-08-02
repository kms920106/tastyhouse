/** 공지사항 화면 헤더 정적 문구 (목록/로딩 공용) */
export const NOTICE_PAGE_COPY = {
  TITLE: "공지사항",
  DESCRIPTION: "서비스 공지사항을 등록하고 관리합니다.",
} as const;

/** 공지사항 사용자 피드백 메시지 (동적/토스트/에러 폴백) */
export const NOTICE_MESSAGE = {
  // 성공 toast
  CREATE_SUCCESS: "공지사항이 등록되었습니다.",
  UPDATE_SUCCESS: "공지사항이 수정되었습니다.",
  DELETE_SUCCESS: "공지사항이 삭제되었습니다.",

  // 에러 폴백
  CREATE_UPDATE_FAILED: "처리 중 오류가 발생했습니다.",
  DELETE_FAILED: "삭제 중 오류가 발생했습니다.",
  INVALID_INPUT: "입력값이 올바르지 않습니다.",
  LIST_LOAD_FAILED: "공지사항 목록을 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.",
  DETAIL_LOAD_FAILED: "공지사항 상세를 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.",
} as const;
