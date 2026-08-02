import "server-only";

import { api } from "@/api/shared/client";
import type { ApiPageRequest, ApiResponse } from "@/api/shared/types";

import type {
  PointBalanceResponse,
  PointDeductRequest,
  PointEarnRequest,
  PointHistoryItemResponse,
  PointHistoryQueryRequest,
} from "./point.dto";

/**
 * 포인트 관리자 API
 */

const ENDPOINT = "/api/points";

export const pointRepository = {
  // 포인트 잔액 조회
  getBalance(memberId: number): Promise<ApiResponse<PointBalanceResponse>> {
    return api.get<PointBalanceResponse>(`${ENDPOINT}/v1/members/${memberId}`);
  },

  // 포인트 이력 조회 (페이징)
  getHistories(
    memberId: number,
    query: PointHistoryQueryRequest,
    pageRequest: ApiPageRequest,
  ): Promise<ApiResponse<PointHistoryItemResponse[]>> {
    return api.get<PointHistoryItemResponse[]>(`${ENDPOINT}/v1/members/${memberId}/histories`, {
      params: { ...query, ...pageRequest },
    });
  },

  // 포인트 수동 적립
  earn(memberId: number, body: PointEarnRequest): Promise<ApiResponse<null>> {
    return api.post<null>(`${ENDPOINT}/v1/members/${memberId}/earn`, body);
  },

  // 포인트 수동 차감
  deduct(memberId: number, body: PointDeductRequest): Promise<ApiResponse<null>> {
    return api.post<null>(`${ENDPOINT}/v1/members/${memberId}/deduct`, body);
  },
};
