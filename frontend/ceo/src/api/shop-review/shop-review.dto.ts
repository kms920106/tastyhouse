/**
 * 점주 리뷰 관리 DTO — `docs/tasks/backend.md` 1-1 ~ 1-11 응답과 1:1 대응.
 *
 * 이 레이어를 벗어나지 않는다(`src/api/AGENTS.md`). UI/feature 는
 * `@/feature/shop-review/domain` 의 도메인 타입만 import 한다.
 */

/**
 * 목록 조회 query 파라미터.
 *
 * 날짜는 `yyyy-MM-dd`. `sortType` 을 생략하면 서버가 점주 저장 설정을 적용하고,
 * 그 설정도 없으면 `LATEST` 로 떨어진다 — 프론트에서 기본값을 채워 보내지 않는다.
 */
export interface ShopReviewListQueryRequest {
  tab?: string;
  startDate?: string;
  endDate?: string;
  rating?: number;
  orderMethod?: string;
  hasImage?: boolean;
  sortType?: string;
}

export interface ShopReviewListItemResponse {
  id: number;
  /** 리뷰 고유 번호 16자리 — `id` 를 0-pad 한 표시용 문자열 */
  reviewNumber: string;
  memberNickname: string;
  totalRating: number;
  content: string;
  /** 없으면 빈 배열 */
  imageUrls: string[];
  productNames: string[];
  /** 주문유형. `order_id` 가 NULL 인 미인증 리뷰면 null */
  orderMethod: string | null;
  orderMethodDescription: string | null;
  /** 차단(게시중단) 여부 */
  hidden: boolean;
  /** 사장님만보기(작성자 비공개) 여부. `hidden` 과 별개 축이다 */
  ownerOnly: boolean;
  /** 미답변이면 null */
  ownerReplyContent: string | null;
  ownerReplyCreatedAt: string | null;
  /** 최근 게시중단 요청 상태. 요청 이력이 없으면 null */
  blindRequestStatus: string | null;
  /** 답변 마감일(yyyy-MM-dd) = 리뷰 작성일 + 30일 */
  replyDeadline: string;
  /** 오늘 기준 **신규 등록** 가능 여부. 이미 답변이 있으면 이 값과 무관하게 수정·삭제 가능 */
  replyable: boolean;
  createdAt: string;
}

export interface ReviewBlindRequestHistoryResponse {
  id: number;
  reason: string;
  reasonDescription: string;
  detailReason: string | null;
  status: string;
  statusDescription: string;
  /** 반려 시에만 채워진다 */
  rejectReason: string | null;
  createdAt: string;
}

/** 상세 응답 — 목록의 전 필드 + 항목별 평점·태그·답변 메타·게시중단 이력 */
export interface ShopReviewDetailResponse extends ShopReviewListItemResponse {
  tasteRating: number | null;
  amountRating: number | null;
  priceRating: number | null;
  atmosphereRating: number | null;
  kindnessRating: number | null;
  hygieneRating: number | null;
  willRevisit: boolean | null;
  tagNames: string[];
  /** 미답변이면 null */
  ownerReplyId: number | null;
  ownerReplyUpdatedAt: string | null;
  /** 배달 평점(1~5). 미평가면 null. 고객 앱에는 노출되지 않는 점주 전용 정보다 */
  deliveryRating: number | null;
  /** 배달 평가 내용. 미평가면 null */
  deliveryComment: string | null;
  /** 최신순 */
  blindRequests: ReviewBlindRequestHistoryResponse[];
}

/** 리뷰 0건인 달은 `averageRating` 이 null 이라 그래프 line 이 끊긴다 */
export interface ShopReviewMonthlyStatResponse {
  /** `yyyy-MM` */
  yearMonth: string;
  averageRating: number | null;
  reviewCount: number;
}

/**
 * 통계 응답.
 *
 * `hasData=false`(최근 180일 리뷰 0건)면 나머지가 전부 null/빈 값으로 내려오고,
 * 화면은 대시보드 영역을 아예 렌더하지 않는다(원문 규격).
 */
export interface ShopReviewStatisticsResponse {
  hasData: boolean;
  averageTotalRating: number | null;
  totalReviewCount: number | null;
  recentReviewCount: number | null;
  /** 키 1~5 가 항상 존재한다(0건이면 값 0) */
  ratingCounts: Record<string, number> | null;
  averageTasteRating: number | null;
  averageAmountRating: number | null;
  averagePriceRating: number | null;
  averageAtmosphereRating: number | null;
  averageKindnessRating: number | null;
  averageHygieneRating: number | null;
  willRevisitPercentage: number | null;
  /** 정확히 6개(오래된 달 → 최신 달) */
  monthlyStats: ShopReviewMonthlyStatResponse[] | null;
}

export interface ShopReviewSortTypeResponse {
  sortType: string;
  sortTypeDescription: string;
  /** 미설정 가게는 null (이때 `sortType` 은 기본값 LATEST) */
  updatedAt: string | null;
}

/** 서버 제약: `@NotBlank`, allowableValues = RECOMMENDED/LATEST/OLDEST */
export interface ShopReviewSortTypeUpdateRequest {
  sortType: string;
}

/** 사장님 답변 등록·수정 공용 본문. 서버 제약은 `@NotBlank` + `@Size(max = 1000)` */
export interface ReviewOwnerReplyUpsertRequest {
  content: string;
}

/** 서버 제약: `reason` `@NotBlank`, `detailReason` `@Size(max = 500)` (ETC 면 필수) */
export interface ReviewBlindRequestCreateRequest {
  reason: string;
  detailReason?: string;
}

export interface ReviewBlindReasonCatalogResponse {
  code: string;
  description: string;
}
