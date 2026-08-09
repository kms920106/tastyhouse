import "server-only";

import { api } from "@/api/shared/client";
import type { ApiPageRequest, ApiResponse } from "@/api/shared/types";

import type { AdminDongItemResponse, AdminDongSearchQueryRequest } from "./region.dto";

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
};
