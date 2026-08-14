import "server-only";

import { api } from "@/api/shared/client";
import type { ApiPageRequest, ApiResponse } from "@/api/shared/types";

import type {
  ReviewBlindReasonCatalogResponse,
  ReviewBlindRequestCreateRequest,
  ReviewOwnerReplyUpsertRequest,
  ShopReviewDetailResponse,
  ShopReviewListItemResponse,
  ShopReviewListQueryRequest,
  ShopReviewSortTypeResponse,
  ShopReviewSortTypeUpdateRequest,
  ShopReviewStatisticsResponse,
} from "./shop-review.dto";

/**
 * 점주용 리뷰 관리 API (transport only)
 *
 * 하위 리소스 경로 규칙은 리소스마다 다르므로(`src/api/AGENTS.md`) 일반화하지 않고
 * `docs/tasks/backend.md` 1-1 ~ 1-11 을 그대로 반영한다.
 * - 리뷰·답변·통계·정렬설정은 전부 부모 `{shopId}` 경로를 유지한다 (`/v1/{shopId}/reviews/...`)
 * - **게시중단 요청 취소만 `{reviewId}` 를 거치지 않는다** (`/v1/{shopId}/reviews/blind-requests/{requestId}/cancel`).
 *   요청 ID 가 이미 리뷰를 특정하므로 경로에 reviewId 를 중복해 넣지 않는 서버 스펙이다.
 * - 사유 카탈로그는 가게에 종속되지 않는 정적 목록이라 `{shopId}` 가 없다.
 */

const ENDPOINT = "/api/shops";

const BLIND_REASON_CATALOG_PATH = `${ENDPOINT}/v1/review-blind-reasons`;

export const shopReviewRepository = {
  getList(
    shopId: number,
    query: ShopReviewListQueryRequest,
    pageRequest: ApiPageRequest,
  ): Promise<ApiResponse<ShopReviewListItemResponse[]>> {
    return api.get<ShopReviewListItemResponse[]>(`${ENDPOINT}/v1/${shopId}/reviews`, {
      params: { ...query, ...pageRequest },
    });
  },

  getDetail(shopId: number, reviewId: number): Promise<ApiResponse<ShopReviewDetailResponse>> {
    return api.get<ShopReviewDetailResponse>(`${ENDPOINT}/v1/${shopId}/reviews/${reviewId}`);
  },

  getStatistics(shopId: number): Promise<ApiResponse<ShopReviewStatisticsResponse>> {
    return api.get<ShopReviewStatisticsResponse>(`${ENDPOINT}/v1/${shopId}/reviews/statistics`);
  },

  getSortType(shopId: number): Promise<ApiResponse<ShopReviewSortTypeResponse>> {
    return api.get<ShopReviewSortTypeResponse>(`${ENDPOINT}/v1/${shopId}/reviews/sort-type`);
  },

  // 설정 행이 없으면 생성, 있으면 갱신하는 upsert 라 POST 가 아니라 PUT 이다.
  updateSortType(shopId: number, body: ShopReviewSortTypeUpdateRequest): Promise<ApiResponse<void>> {
    return api.put<void>(`${ENDPOINT}/v1/${shopId}/reviews/sort-type`, body);
  },

  // 등록 POST 는 생성된 PK 만 반환하는 규칙을 따른다.
  createOwnerReply(
    shopId: number,
    reviewId: number,
    body: ReviewOwnerReplyUpsertRequest,
  ): Promise<ApiResponse<number>> {
    return api.post<number>(`${ENDPOINT}/v1/${shopId}/reviews/${reviewId}/owner-reply`, body);
  },

  updateOwnerReply(shopId: number, reviewId: number, body: ReviewOwnerReplyUpsertRequest): Promise<ApiResponse<void>> {
    return api.put<void>(`${ENDPOINT}/v1/${shopId}/reviews/${reviewId}/owner-reply`, body);
  },

  // 리뷰당 답변은 1건뿐이라 답변 ID 없이 리뷰 경로만으로 삭제한다.
  deleteOwnerReply(shopId: number, reviewId: number): Promise<ApiResponse<void>> {
    return api.delete<void>(`${ENDPOINT}/v1/${shopId}/reviews/${reviewId}/owner-reply`);
  },

  createBlindRequest(
    shopId: number,
    reviewId: number,
    body: ReviewBlindRequestCreateRequest,
  ): Promise<ApiResponse<number>> {
    return api.post<number>(`${ENDPOINT}/v1/${shopId}/reviews/${reviewId}/blind-requests`, body);
  },

  // 본문 없는 PATCH — 상태 전이만 지시하고 응답은 ApiResponse<Void> 다.
  // reviewId 를 거치지 않는 경로인 점에 주의(위 주석 참고).
  cancelBlindRequest(shopId: number, requestId: number): Promise<ApiResponse<void>> {
    return api.patch<void>(`${ENDPOINT}/v1/${shopId}/reviews/blind-requests/${requestId}/cancel`);
  },

  getBlindReasons(): Promise<ApiResponse<ReviewBlindReasonCatalogResponse[]>> {
    return api.get<ReviewBlindReasonCatalogResponse[]>(BLIND_REASON_CATALOG_PATH);
  },
};
