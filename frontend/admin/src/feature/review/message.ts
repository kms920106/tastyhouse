/** 리뷰 화면 헤더 정적 문구 (목록/로딩 공용) */
export const REVIEW_PAGE_COPY = {
  TITLE: "리뷰",
  DESCRIPTION: "매장 리뷰와 댓글·답글을 조회하고 숨김·삭제 처리합니다.",
} as const;

/** 리뷰 사용자 피드백 메시지 (동적/토스트/에러 폴백) */
export const REVIEW_MESSAGE = {
  // 성공 toast
  HIDDEN_SUCCESS: "리뷰가 숨김 처리되었습니다.",
  VISIBLE_SUCCESS: "리뷰가 노출 처리되었습니다.",
  DELETE_SUCCESS: "리뷰가 삭제되었습니다.",
  COMMENT_HIDDEN_SUCCESS: "댓글이 숨김 처리되었습니다.",
  COMMENT_VISIBLE_SUCCESS: "댓글이 노출 처리되었습니다.",
  COMMENT_DELETE_SUCCESS: "댓글이 삭제되었습니다.",
  REPLY_HIDDEN_SUCCESS: "답글이 숨김 처리되었습니다.",
  REPLY_VISIBLE_SUCCESS: "답글이 노출 처리되었습니다.",
  REPLY_DELETE_SUCCESS: "답글이 삭제되었습니다.",

  // 에러 폴백
  INVALID_INPUT: "입력값이 올바르지 않습니다.",
  HIDDEN_FAILED: "숨김/노출 처리 중 오류가 발생했습니다.",
  DELETE_FAILED: "삭제 중 오류가 발생했습니다.",
  COMMENT_HIDDEN_FAILED: "댓글 숨김/노출 처리 중 오류가 발생했습니다.",
  COMMENT_DELETE_FAILED: "댓글 삭제 중 오류가 발생했습니다.",
  REPLY_HIDDEN_FAILED: "답글 숨김/노출 처리 중 오류가 발생했습니다.",
  REPLY_DELETE_FAILED: "답글 삭제 중 오류가 발생했습니다.",
  LIST_LOAD_FAILED: "리뷰 목록을 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.",
  DETAIL_LOAD_FAILED: "리뷰 상세를 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.",
  COMMENTS_LOAD_FAILED: "댓글 목록을 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.",
} as const;
