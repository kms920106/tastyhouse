// 리뷰 게시중단 요청 도메인 모델 — UI 와 api/review-blind-request.service 가 공유한다.

export type ReviewBlindRequestStatus = "PENDING" | "APPROVED" | "REJECTED" | "CANCELED" | "EXPIRED" | "DELETED";

export type ReviewBlindReason = "ADVERTISEMENT" | "PROFANITY" | "IRRELEVANT" | "PRIVACY" | "ETC";

export interface ReviewBlindRequestListItem {
  id: number;
  reviewId: number;
  shopId: number;
  shopName: string;
  reason: ReviewBlindReason;
  reasonDescription: string;
  status: ReviewBlindRequestStatus;
  statusDescription: string;
  reviewContent: string;
  reviewTotalRating: number;
  createdAt: string;
  /** 재노출 예정일시. APPROVED 가 아니면 null. */
  blindUntil: string | null;
}

export interface ReviewBlindRequestDetail extends ReviewBlindRequestListItem {
  detailReason: string | null;
  rejectReason: string | null;
  reviewImageUrls: string[];
  /** 점주가 첨부한 증빙 서류 URL 목록 (이미지·PDF 혼재라 링크로만 연다). */
  attachmentUrls: string[];
  reviewMemberNickname: string;
  reviewHidden: boolean;
  reviewCreatedAt: string;
}
