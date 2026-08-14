// 리뷰 게시중단 요청 도메인 모델 — UI 와 api/review-blind-request.service 가 공유한다.

export type ReviewBlindRequestStatus = "PENDING" | "APPROVED" | "REJECTED" | "CANCELED";

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
}

export interface ReviewBlindRequestDetail extends ReviewBlindRequestListItem {
  detailReason: string | null;
  rejectReason: string | null;
  reviewImageUrls: string[];
  reviewMemberNickname: string;
  reviewHidden: boolean;
  reviewCreatedAt: string;
}
