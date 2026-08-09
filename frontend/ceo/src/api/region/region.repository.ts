import "server-only";

import { api } from "@/api/shared/client";
import type { ApiPageRequest, ApiResponse } from "@/api/shared/types";

import type {
  AdminDongBoundaryQueryRequest,
  AdminDongBoundaryResponse,
  AdminDongItemResponse,
  AdminDongSearchQueryRequest,
  AdminDongTreeQueryRequest,
  AdminDongTreeResponse,
} from "./region.dto";

/**
 * 행정동(지역) 조회 API
 *
 * 배달가능지역으로 등록할 행정동을 검색한다. 서버는 `is_active = true` 인 행만 내려주며
 * `regionName` 을 "시도 시군구 읍면동" 으로 조립해 완성된 형태로 준다.
 */

const ENDPOINT = "/api/admin-dongs";

export const regionRepository = {
  searchAdminDongs(
    query: AdminDongSearchQueryRequest,
    pageRequest: ApiPageRequest,
  ): Promise<ApiResponse<AdminDongItemResponse[]>> {
    return api.get<AdminDongItemResponse[]>(`${ENDPOINT}/v1`, {
      params: { ...query, ...pageRequest },
    });
  },

  /**
   * 행정동 계층 3단 lazy 조회.
   *
   * 가게 소유권과 무관하며 점주 인증만 있으면 호출할 수 있다.
   */
  getAdminDongTree(query: AdminDongTreeQueryRequest): Promise<ApiResponse<AdminDongTreeResponse>> {
    return api.get<AdminDongTreeResponse>(`${ENDPOINT}/v1/tree`, { params: query });
  },

  /**
   * 행정동 경계 조회.
   *
   * bbox(`swLat`/`swLng`/`neLat`/`neLng`)와 `adminDongIds` 는 배타적이다 — 둘 다 보내거나
   * 둘 다 빼면 서버가 400 을 낸다. 호출부에서 한 가지만 채워 보낸다.
   */
  getAdminDongBoundaries(query: AdminDongBoundaryQueryRequest): Promise<ApiResponse<AdminDongBoundaryResponse>> {
    return api.get<AdminDongBoundaryResponse>(`${ENDPOINT}/v1/boundaries`, { params: query });
  },
};
