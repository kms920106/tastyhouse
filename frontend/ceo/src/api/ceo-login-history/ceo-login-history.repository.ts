import "server-only";

import { api } from "@/api/shared/client";
import type { ApiPageRequest, ApiResponse } from "@/api/shared/types";

import type { CeoLoginHistoryItemResponse, CeoLoginHistoryListQueryRequest } from "./ceo-login-history.dto";

/**
 * 점주 개인정보 접속기록(로그인 이력) 조회 API (transport only)
 *
 * `docs/tasks/backend.md` §2-1 을 그대로 반영한다. 계정 단위 기록이라 경로에
 * `{shopId}` 가 없고 `me` 로 잡혀 있다 — 인가는 토큰의 `ceoId` 로만 필터하는 것 자체다.
 */

const ENDPOINT = "/api/ceos";

export const ceoLoginHistoryRepository = {
  getList(
    query: CeoLoginHistoryListQueryRequest,
    pageRequest: ApiPageRequest,
  ): Promise<ApiResponse<CeoLoginHistoryItemResponse[]>> {
    return api.get<CeoLoginHistoryItemResponse[]>(`${ENDPOINT}/v1/me/login-histories`, {
      params: { ...query, ...pageRequest },
    });
  },
};
