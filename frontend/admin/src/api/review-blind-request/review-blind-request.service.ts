import "server-only";

import type { ApiPageRequest, ApiResponse } from "@/api/shared/types";
import type {
  ReviewBlindReason,
  ReviewBlindRequestDetail,
  ReviewBlindRequestListItem,
  ReviewBlindRequestStatus,
} from "@/feature/review-blind-request/domain";

import type {
  ReviewBlindRequestListItemResponse,
  ReviewBlindRequestListQueryRequest,
} from "./review-blind-request.dto";
import { reviewBlindRequestRepository } from "./review-blind-request.repository";

function toListItem(item: ReviewBlindRequestListItemResponse): ReviewBlindRequestListItem {
  return {
    id: item.id,
    reviewId: item.reviewId,
    shopId: item.shopId,
    shopName: item.shopName,
    reason: item.reason as ReviewBlindReason,
    reasonDescription: item.reasonDescription,
    status: item.status as ReviewBlindRequestStatus,
    statusDescription: item.statusDescription,
    reviewContent: item.reviewContent,
    reviewTotalRating: item.reviewTotalRating,
    createdAt: item.createdAt,
  };
}

export const reviewBlindRequestService = {
  // 게시중단 요청 목록 조회 — 도메인 반환
  async getBlindRequests(
    query: ReviewBlindRequestListQueryRequest,
    pageRequest: ApiPageRequest,
  ): Promise<ApiResponse<ReviewBlindRequestListItem[]>> {
    const res = await reviewBlindRequestRepository.getList(query, pageRequest);
    return {
      ...res,
      data: res.data?.map(toListItem),
    };
  },

  // 게시중단 요청 상세 조회 — 도메인 반환
  async getBlindRequest(id: number): Promise<ApiResponse<ReviewBlindRequestDetail>> {
    const res = await reviewBlindRequestRepository.getDetail(id);
    if (!res.data) return { ...res, data: undefined };
    return {
      ...res,
      data: {
        ...toListItem(res.data),
        detailReason: res.data.detailReason,
        rejectReason: res.data.rejectReason,
        reviewImageUrls: res.data.reviewImageUrls,
        reviewMemberNickname: res.data.reviewMemberNickname,
        reviewHidden: res.data.reviewHidden,
        reviewCreatedAt: res.data.reviewCreatedAt,
      },
    };
  },
};
