// 리뷰 게시중단 요청 심사 API 요청/응답 DTO (Review Blind Request Admin — /api/reviews/v1/blind-requests)
// DTO 는 이 계층 밖으로 나가지 않는다. UI/feature 는 @/feature/review-blind-request/domain 을 사용한다.

// 게시중단 요청 목록 조회 쿼리
export interface ReviewBlindRequestListQueryRequest {
  shopId?: number;
  status?: string;
  reason?: string;
  startDate?: string;
  endDate?: string;
}

// 게시중단 요청 목록 항목
export interface ReviewBlindRequestListItemResponse {
  id: number;
  reviewId: number;
  shopId: number;
  shopName: string;
  reason: string;
  reasonDescription: string;
  status: string;
  statusDescription: string;
  reviewContent: string;
  reviewTotalRating: number;
  createdAt: string;
  /** 재노출 예정일시. APPROVED 가 아니면 null. */
  blindUntil: string | null;
}

// 게시중단 요청 상세 (목록 필드 + 심사 판단에 필요한 리뷰 원문/사진)
export interface ReviewBlindRequestDetailResponse extends ReviewBlindRequestListItemResponse {
  detailReason: string | null;
  rejectReason: string | null;
  reviewImageUrls: string[];
  /** 점주가 첨부한 증빙 서류 URL 목록 (이미지·PDF 혼재). */
  attachmentUrls: string[];
  reviewMemberNickname: string;
  reviewHidden: boolean;
  reviewCreatedAt: string;
}

// 게시중단 요청 반려 본문
export interface ReviewBlindRequestRejectRequest {
  rejectReason: string;
}
