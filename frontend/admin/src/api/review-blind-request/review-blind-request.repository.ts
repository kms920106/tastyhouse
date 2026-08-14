import "server-only";

import { api } from "@/api/shared/client";
import type { ApiPageRequest, ApiResponse } from "@/api/shared/types";

import type {
  ReviewBlindRequestDetailResponse,
  ReviewBlindRequestListItemResponse,
  ReviewBlindRequestListQueryRequest,
  ReviewBlindRequestRejectRequest,
} from "./review-blind-request.dto";

/**
 * 리뷰 게시중단 요청 심사 관리자 API
 */

const ENDPOINT = "/api/reviews/v1/blind-requests";

export const reviewBlindRequestRepository = {
  // 게시중단 요청 목록 조회
  getList(
    query: ReviewBlindRequestListQueryRequest,
    pageRequest: ApiPageRequest,
  ): Promise<ApiResponse<ReviewBlindRequestListItemResponse[]>> {
    return api.get<ReviewBlindRequestListItemResponse[]>(ENDPOINT, {
      params: { ...query, ...pageRequest },
    });
  },

  // 게시중단 요청 상세 조회
  getDetail(id: number): Promise<ApiResponse<ReviewBlindRequestDetailResponse>> {
    return api.get<ReviewBlindRequestDetailResponse>(`${ENDPOINT}/${id}`);
  },

  // 게시중단 요청 승인 (리뷰가 고객 화면에서 즉시 숨겨진다)
  approve(id: number): Promise<ApiResponse<null>> {
    return api.put<null>(`${ENDPOINT}/${id}/approve`);
  },

  // 게시중단 요청 반려 (리뷰는 노출 상태를 유지한다)
  reject(id: number, body: ReviewBlindRequestRejectRequest): Promise<ApiResponse<null>> {
    return api.put<null>(`${ENDPOINT}/${id}/reject`, body);
  },
};
