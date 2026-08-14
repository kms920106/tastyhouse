// 점주 리뷰 관리 도메인 모델 — UI 와 `api/shop-review/shop-review.service` 가 공유한다.
// DTO(`*.dto.ts`)는 이 경계를 넘지 않으므로 UI 는 항상 이 파일의 타입만 import 한다.

/** 목록 탭. `BLINDED` 는 게시중단(차단)된 리뷰만 본다 */
export type ShopReviewTab = "ALL" | "UNANSWERED" | "BLINDED";

/** 조회·표시 정렬. 필터용과 앱 노출 설정용이 같은 값 집합을 쓴다 */
export type ReviewSortType = "RECOMMENDED" | "LATEST" | "OLDEST";

/** 게시중단 요청 사유. 카탈로그는 서버에서 받지만 폼 스키마가 값 집합을 알아야 한다 */
export type ReviewBlindReason = "ADVERTISEMENT" | "PROFANITY" | "IRRELEVANT" | "PRIVACY" | "ETC";

export interface ShopReviewListItem {
  id: number;
  reviewNumber: string;
  memberNickname: string;
  totalRating: number;
  content: string;
  imageUrls: string[];
  productNames: string[];
  /** 미인증 리뷰(주문과 연결되지 않음)면 null */
  orderMethod: string | null;
  orderMethodDescription: string | null;
  hidden: boolean;
  ownerReplyContent: string | null;
  ownerReplyCreatedAt: string | null;
  blindRequestStatus: string | null;
  createdAt: string;
}

export interface ReviewBlindRequestHistory {
  id: number;
  reason: string;
  reasonDescription: string;
  detailReason: string | null;
  status: string;
  statusDescription: string;
  rejectReason: string | null;
  createdAt: string;
}

export interface ShopReviewDetail extends ShopReviewListItem {
  tasteRating: number | null;
  amountRating: number | null;
  priceRating: number | null;
  atmosphereRating: number | null;
  kindnessRating: number | null;
  hygieneRating: number | null;
  willRevisit: boolean | null;
  tagNames: string[];
  ownerReplyId: number | null;
  ownerReplyUpdatedAt: string | null;
  /** 최신순 */
  blindRequests: ReviewBlindRequestHistory[];
}

export interface ShopReviewMonthlyStat {
  /** `yyyy-MM` */
  yearMonth: string;
  /** 리뷰 0건인 달은 null — 그래프에서 line 을 끊는다 */
  averageRating: number | null;
  reviewCount: number;
}

/** 별점 분포 한 행. 키 1~5 가 항상 존재하므로 0건도 count 0 으로 들어온다 */
export interface ShopReviewRatingCount {
  rating: number;
  count: number;
}

/**
 * 통계.
 *
 * `hasData=false`(최근 180일 리뷰 0건)면 화면이 대시보드를 통째로 렌더하지 않으므로
 * 나머지 필드는 모두 null/빈 배열로 정규화한다 — UI 가 개별 필드를 방어하지 않아도 되게 한다.
 */
export interface ShopReviewStatistics {
  hasData: boolean;
  averageTotalRating: number | null;
  totalReviewCount: number | null;
  recentReviewCount: number | null;
  ratingCounts: ShopReviewRatingCount[];
  averageTasteRating: number | null;
  averageAmountRating: number | null;
  averagePriceRating: number | null;
  averageAtmosphereRating: number | null;
  averageKindnessRating: number | null;
  averageHygieneRating: number | null;
  willRevisitPercentage: number | null;
  monthlyStats: ShopReviewMonthlyStat[];
}

export interface ShopReviewSortTypeSetting {
  sortType: string;
  sortTypeDescription: string;
  /** 미설정이면 null (이때 `sortType` 은 서버 기본값 LATEST) */
  updatedAt: string | null;
}

export interface ReviewBlindReasonOption {
  code: string;
  description: string;
}
