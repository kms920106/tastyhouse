/** 게시중단 요청 심사 화면 헤더 정적 문구 (목록/로딩 공용) */
export const REVIEW_BLIND_REQUEST_PAGE_COPY = {
  TITLE: "게시중단 요청 심사",
  DESCRIPTION: "점주가 올린 리뷰 게시중단 요청을 확인하고 승인·반려합니다.",
} as const;

/** 승인/반려 다이얼로그 문구 */
export const REVIEW_BLIND_REQUEST_DIALOG_COPY = {
  APPROVE_TITLE: "게시중단 요청을 승인하시겠습니까?",
  APPROVE_DESCRIPTION:
    "승인하면 이 리뷰는 고객 화면에서 즉시 숨겨집니다. 되돌리려면 리뷰 관리 화면에서 노출로 전환해야 합니다.",
  REJECT_TITLE: "게시중단 요청 반려",
  REJECT_DESCRIPTION: "반려 사유를 입력해 주세요. 점주의 요청처리 현황 화면에 그대로 노출됩니다.",
  REJECT_REASON_LABEL: "반려 사유",
  REJECT_REASON_PLACEHOLDER: "점주에게 그대로 노출되니 읽고 이해할 수 있는 문장으로 작성해주세요.",
} as const;

/** 게시중단 요청 상세 Sheet 문구 */
export const REVIEW_BLIND_REQUEST_DETAIL_COPY = {
  TITLE: "게시중단 요청 상세",
  DESCRIPTION: "요청 사유와 리뷰 원문을 확인하고 심사합니다.",
  NO_IMAGE: "첨부된 리뷰 사진이 없습니다.",
  IMAGE_LOAD_FAILED: "이미지를 불러오지 못했습니다.",
} as const;

/** 게시중단 요청 사용자 피드백 메시지 (동적/토스트/에러 폴백) */
export const REVIEW_BLIND_REQUEST_MESSAGE = {
  // 성공 toast
  APPROVE_SUCCESS: "게시중단 요청이 승인되었습니다. 리뷰가 고객 화면에서 숨겨집니다.",
  REJECT_SUCCESS: "게시중단 요청이 반려되었습니다.",

  // 에러 폴백
  INVALID_INPUT: "입력값이 올바르지 않습니다.",
  APPROVE_FAILED: "승인 처리 중 오류가 발생했습니다.",
  REJECT_FAILED: "반려 처리 중 오류가 발생했습니다.",
  LIST_LOAD_FAILED: "게시중단 요청 목록을 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.",
  DETAIL_LOAD_FAILED: "게시중단 요청 상세를 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.",
  EMPTY_LIST: "조건에 해당하는 게시중단 요청이 없습니다.",
} as const;

/**
 * 서버 errorCode → 사용자 문구.
 * 두 코드 모두 다른 관리자가 먼저 처리한 상황이므로, 호출부는 처리 후 목록을 갱신해 최신 상태를 보여준다.
 */
export const REVIEW_BLIND_REQUEST_ERROR_MESSAGE: Record<string, string> = {
  REVIEW_BLIND_REQUEST_NOT_FOUND: "게시중단 요청을 찾을 수 없습니다.",
  REVIEW_BLIND_REQUEST_NOT_PENDING: "이미 처리된 요청입니다. 목록을 새로 불러와주세요.",
};
