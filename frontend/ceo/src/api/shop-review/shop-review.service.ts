import "server-only";

import type { ApiPageRequest, ApiResponse } from "@/api/shared/types";
import type {
  ReviewBlindReasonOption,
  ReviewBlindRequestHistory,
  ShopReviewDetail,
  ShopReviewListItem,
  ShopReviewRatingCount,
  ShopReviewSortTypeSetting,
  ShopReviewStatistics,
} from "@/feature/shop-review/domain";

import type {
  ReviewBlindRequestHistoryResponse,
  ShopReviewDetailResponse,
  ShopReviewListItemResponse,
  ShopReviewListQueryRequest,
  ShopReviewSortTypeResponse,
  ShopReviewStatisticsResponse,
} from "./shop-review.dto";
import { shopReviewRepository } from "./shop-review.repository";

/** 별점 분포에 항상 존재해야 하는 키. 서버가 1~5 를 보장하지만 빠져도 0 으로 채워 그래프가 무너지지 않게 한다 */
const RATING_SCALE = [5, 4, 3, 2, 1] as const;

// 날짜 문자열은 그대로 둔다 — 렌더 시점에 formatDateTime 으로 포맷한다.
function toListItem(item: ShopReviewListItemResponse): ShopReviewListItem {
  return {
    id: item.id,
    reviewNumber: item.reviewNumber,
    memberNickname: item.memberNickname,
    totalRating: item.totalRating,
    content: item.content,
    // 사진·메뉴명은 없으면 빈 배열로 내려오는 것이 서버 계약이다(`docs/tasks/backend.md` 1-1).
    imageUrls: item.imageUrls,
    productNames: item.productNames,
    orderMethod: item.orderMethod,
    orderMethodDescription: item.orderMethodDescription,
    hidden: item.hidden,
    ownerOnly: item.ownerOnly,
    ownerReplyContent: item.ownerReplyContent,
    ownerReplyCreatedAt: item.ownerReplyCreatedAt,
    blindRequestStatus: item.blindRequestStatus,
    replyDeadline: item.replyDeadline,
    replyable: item.replyable,
    createdAt: item.createdAt,
  };
}

function toBlindRequestHistory(item: ReviewBlindRequestHistoryResponse): ReviewBlindRequestHistory {
  return {
    id: item.id,
    reason: item.reason,
    reasonDescription: item.reasonDescription,
    detailReason: item.detailReason,
    status: item.status,
    statusDescription: item.statusDescription,
    rejectReason: item.rejectReason,
    createdAt: item.createdAt,
  };
}

function toDetail(item: ShopReviewDetailResponse): ShopReviewDetail {
  return {
    ...toListItem(item),
    tasteRating: item.tasteRating,
    amountRating: item.amountRating,
    priceRating: item.priceRating,
    atmosphereRating: item.atmosphereRating,
    kindnessRating: item.kindnessRating,
    hygieneRating: item.hygieneRating,
    willRevisit: item.willRevisit,
    tagNames: item.tagNames,
    ownerReplyId: item.ownerReplyId,
    ownerReplyUpdatedAt: item.ownerReplyUpdatedAt,
    deliveryRating: item.deliveryRating,
    deliveryComment: item.deliveryComment,
    blindRequests: item.blindRequests.map(toBlindRequestHistory),
  };
}

/**
 * `Map<Integer, Long>` 이 JSON 에서는 문자열 키 객체로 오므로 5→1 내림차순 배열로 편다.
 *
 * 배열로 바꾸는 이유는 렌더 순서를 서버 응답의 키 순서에 맡기지 않기 위함이다 —
 * 별점 분포는 항상 5★ 부터 보여야 하는데 객체 키 순서는 계약이 아니다.
 */
function toRatingCounts(counts: Record<string, number> | null): ShopReviewRatingCount[] {
  if (!counts) return [];
  return RATING_SCALE.map((rating) => ({ rating, count: counts[String(rating)] ?? 0 }));
}

/**
 * `hasData=false` 면 나머지를 전부 비운다.
 *
 * 서버도 null 로 내려주기로 돼 있지만, 화면이 "데이터 없음"을 `hasData` 하나로만 판정하도록
 * 여기서 정규화해 둔다 — 두 소스가 어긋나 반쯤 채워진 대시보드가 뜨는 것을 막는다.
 */
function toStatistics(item: ShopReviewStatisticsResponse): ShopReviewStatistics {
  if (!item.hasData) {
    return {
      hasData: false,
      averageTotalRating: null,
      totalReviewCount: null,
      recentReviewCount: null,
      ratingCounts: [],
      averageTasteRating: null,
      averageAmountRating: null,
      averagePriceRating: null,
      averageAtmosphereRating: null,
      averageKindnessRating: null,
      averageHygieneRating: null,
      willRevisitPercentage: null,
      monthlyStats: [],
    };
  }

  return {
    hasData: true,
    averageTotalRating: item.averageTotalRating,
    totalReviewCount: item.totalReviewCount,
    recentReviewCount: item.recentReviewCount,
    ratingCounts: toRatingCounts(item.ratingCounts),
    averageTasteRating: item.averageTasteRating,
    averageAmountRating: item.averageAmountRating,
    averagePriceRating: item.averagePriceRating,
    averageAtmosphereRating: item.averageAtmosphereRating,
    averageKindnessRating: item.averageKindnessRating,
    averageHygieneRating: item.averageHygieneRating,
    willRevisitPercentage: item.willRevisitPercentage,
    monthlyStats: (item.monthlyStats ?? []).map((stat) => ({
      yearMonth: stat.yearMonth,
      averageRating: stat.averageRating,
      reviewCount: stat.reviewCount,
    })),
  };
}

function toSortTypeSetting(item: ShopReviewSortTypeResponse): ShopReviewSortTypeSetting {
  return {
    sortType: item.sortType,
    sortTypeDescription: item.sortTypeDescription,
    updatedAt: item.updatedAt,
  };
}

export const shopReviewService = {
  // 목록 조회 — pagination 은 래퍼 그대로 넘겨 페이지네이션이 totalPages 를 쓰게 한다.
  async getList(
    shopId: number,
    query: ShopReviewListQueryRequest,
    pageRequest: ApiPageRequest,
  ): Promise<ApiResponse<ShopReviewListItem[]>> {
    const result = await shopReviewRepository.getList(shopId, query, pageRequest);
    if (result.error || !result.data) return { ...result, data: undefined };

    return { ...result, data: result.data.map(toListItem) };
  },

  async getDetail(shopId: number, reviewId: number): Promise<ApiResponse<ShopReviewDetail>> {
    const result = await shopReviewRepository.getDetail(shopId, reviewId);
    if (result.error || !result.data) return { ...result, data: undefined };

    return { ...result, data: toDetail(result.data) };
  },

  async getStatistics(shopId: number): Promise<ApiResponse<ShopReviewStatistics>> {
    const result = await shopReviewRepository.getStatistics(shopId);
    if (result.error || !result.data) return { ...result, data: undefined };

    return { ...result, data: toStatistics(result.data) };
  },

  async getSortType(shopId: number): Promise<ApiResponse<ShopReviewSortTypeSetting>> {
    const result = await shopReviewRepository.getSortType(shopId);
    if (result.error || !result.data) return { ...result, data: undefined };

    return { ...result, data: toSortTypeSetting(result.data) };
  },

  async getBlindReasons(): Promise<ApiResponse<ReviewBlindReasonOption[]>> {
    const result = await shopReviewRepository.getBlindReasons();
    if (result.error || !result.data) return { ...result, data: undefined };

    return {
      ...result,
      data: result.data.map((reason) => ({ code: reason.code, description: reason.description })),
    };
  },
};
