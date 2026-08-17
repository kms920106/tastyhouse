import type { ReviewBlindReason, ReviewBlindRequestStatus } from "./domain";

/** 반려 사유 최대 길이 (backend `@Size(max = 500)` 과 동일) */
export const REJECT_REASON_MAX = 500;

/** 심사 상태 카탈로그 — 필터 Select 옵션 순서를 겸한다. */
export const REVIEW_BLIND_REQUEST_STATUS_OPTIONS = [
  "PENDING",
  "APPROVED",
  "REJECTED",
  "CANCELED",
  "EXPIRED",
  "DELETED",
] as const;

export const REVIEW_BLIND_REQUEST_STATUS_LABEL: Record<ReviewBlindRequestStatus, string> = {
  PENDING: "심사 대기",
  APPROVED: "승인",
  REJECTED: "반려",
  CANCELED: "취소",
  EXPIRED: "재노출",
  DELETED: "삭제",
};

/** 게시중단 요청 사유 카탈로그 (backend `ReviewBlindReason`) */
export const REVIEW_BLIND_REASON_OPTIONS = ["ADVERTISEMENT", "PROFANITY", "IRRELEVANT", "PRIVACY", "ETC"] as const;

export const REVIEW_BLIND_REASON_LABEL: Record<ReviewBlindReason, string> = {
  ADVERTISEMENT: "광고·홍보",
  PROFANITY: "욕설·비방",
  IRRELEVANT: "주문과 무관",
  PRIVACY: "개인정보 노출",
  ETC: "기타",
};

/** 목록 리뷰 미리보기 최대 노출 길이 — 넘으면 말줄임한다. */
export const REVIEW_CONTENT_PREVIEW_MAX = 40;
