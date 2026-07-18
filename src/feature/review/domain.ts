// 리뷰 도메인 모델 — UI 와 api/review.service 가 공유한다.

export interface ReviewListItem {
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

export interface ReviewDetail {
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

export interface ReviewReply {
  id: number;
  memberId: number;
  memberNickname: string;
  replyToMemberId: number;
  replyToMemberNickname: string;
  content: string;
  hidden: boolean;
  createdAt: string;
}

export interface ReviewComment {
  id: number;
  memberId: number;
  memberNickname: string;
  content: string;
  hidden: boolean;
  createdAt: string;
  replies: ReviewReply[];
}
