// 리뷰 관리자 API 요청/응답 DTO (Review Admin — /api/reviews)
// DTO 는 이 계층 밖으로 나가지 않는다. UI/feature 는 @/feature/review/domain 을 사용한다.

// 리뷰 목록 조회 쿼리
export interface ReviewListQueryRequest {
  shopId?: number;
  productId?: number;
  memberId?: number;
  hidden?: boolean;
  content?: string;
  minRating?: number;
  maxRating?: number;
}

// 리뷰 목록 항목
export interface ReviewListItemResponse {
  id: number;
  shopId: number;
  productId: number;
  memberId: number;
  memberNickname: string;
  totalRating: number;
  content: string;
  hidden: boolean;
  createdAt: string;
}

// 리뷰 상세
export interface ReviewManagementDetailResponse {
  id: number;
  shopId: number;
  shopName: string;
  stationName: string;
  content: string;
  totalRating: number;
  tasteRating: number;
  amountRating: number;
  priceRating: number;
  atmosphereRating: number;
  kindnessRating: number;
  hygieneRating: number;
  willRevisit: boolean;
  hidden: boolean;
  memberId: number;
  memberNickname: string;
  memberProfileImageUrl: string | null;
  createdAt: string;
  imageUrls: string[];
  tagNames: string[];
}

// 숨김/노출 전환 요청 (리뷰/댓글/답글 공용)
export interface ReviewHiddenRequest {
  hidden: boolean;
}

// 리뷰 답글 목록 항목
export interface ReviewReplyListItemResponse {
  id: number;
  memberId: number;
  memberNickname: string;
  replyToMemberId: number;
  replyToMemberNickname: string;
  content: string;
  hidden: boolean;
  createdAt: string;
}

// 리뷰 댓글 목록 항목 (답글 포함)
export interface ReviewCommentListItemResponse {
  id: number;
  memberId: number;
  memberNickname: string;
  content: string;
  hidden: boolean;
  createdAt: string;
  replies: ReviewReplyListItemResponse[];
}
