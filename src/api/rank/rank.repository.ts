import "server-only";

import { api } from "@/api/shared/client";
import type { ApiResponse } from "@/api/shared/types";

import type {
  RankAggregationRequest,
  RankMemberListItemResponse,
  RankMemberListQueryRequest,
  RankPeriodDetailResponse,
  RankPeriodListItemResponse,
  RankPeriodSaveRequest,
  RankPrizeDetailResponse,
  RankPrizeListItemResponse,
  RankPrizeSaveRequest,
} from "./rank.dto";

/**
 * 랭킹 관리자 API
 */

const ENDPOINT = "/api/ranks";

export const rankRepository = {
  // 회원 랭킹 목록 조회
  getMembers(query: RankMemberListQueryRequest): Promise<ApiResponse<RankMemberListItemResponse[]>> {
    return api.get<RankMemberListItemResponse[]>(`${ENDPOINT}/v1/members`, { params: query });
  },

  // 랭킹 집계 수동 실행 (body 전부 optional — 빈 {} 허용)
  aggregate(body: RankAggregationRequest): Promise<ApiResponse<null>> {
    return api.post<null>(`${ENDPOINT}/v1/aggregations`, body);
  },

  // 랭킹 기간 목록 조회 (페이징 없음)
  getPeriods(): Promise<ApiResponse<RankPeriodListItemResponse[]>> {
    return api.get<RankPeriodListItemResponse[]>(`${ENDPOINT}/v1/periods`);
  },

  // 랭킹 기간 등록
  createPeriod(body: RankPeriodSaveRequest): Promise<ApiResponse<number>> {
    return api.post<number>(`${ENDPOINT}/v1/periods`, body);
  },

  // 랭킹 기간 상세 조회
  getPeriod(id: number): Promise<ApiResponse<RankPeriodDetailResponse>> {
    return api.get<RankPeriodDetailResponse>(`${ENDPOINT}/v1/periods/${id}`);
  },

  // 랭킹 기간 수정
  updatePeriod(id: number, body: RankPeriodSaveRequest): Promise<ApiResponse<null>> {
    return api.put<null>(`${ENDPOINT}/v1/periods/${id}`, body);
  },

  // 랭킹 기간 삭제 (하드 삭제 — 소속 경품도 함께 삭제)
  deletePeriod(id: number): Promise<ApiResponse<null>> {
    return api.delete<null>(`${ENDPOINT}/v1/periods/${id}`);
  },

  // 특정 기간의 경품 목록 조회 (등수 오름차순)
  getPrizes(periodId: number): Promise<ApiResponse<RankPrizeListItemResponse[]>> {
    return api.get<RankPrizeListItemResponse[]>(`${ENDPOINT}/v1/periods/${periodId}/prizes`);
  },

  // 경품 등록 (기간 ID 기준)
  createPrize(periodId: number, body: RankPrizeSaveRequest): Promise<ApiResponse<number>> {
    return api.post<number>(`${ENDPOINT}/v1/periods/${periodId}/prizes`, body);
  },

  // 경품 단건 상세 조회 (prizeId 는 전역 유니크)
  getPrize(prizeId: number): Promise<ApiResponse<RankPrizeDetailResponse>> {
    return api.get<RankPrizeDetailResponse>(`${ENDPOINT}/v1/prizes/${prizeId}`);
  },

  // 경품 수정
  updatePrize(prizeId: number, body: RankPrizeSaveRequest): Promise<ApiResponse<null>> {
    return api.put<null>(`${ENDPOINT}/v1/prizes/${prizeId}`, body);
  },

  // 경품 삭제
  deletePrize(prizeId: number): Promise<ApiResponse<null>> {
    return api.delete<null>(`${ENDPOINT}/v1/prizes/${prizeId}`);
  },
};
