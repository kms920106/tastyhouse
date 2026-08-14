import "server-only";

import { api } from "@/api/shared/client";
import type { ApiPageRequest, ApiResponse } from "@/api/shared/types";

import type {
  CeoShopAccessHistoryItemResponse,
  CeoShopAccessHistoryListQueryRequest,
} from "./ceo-shop-access-history.dto";

/**
 * 점주 시스템 접근권한 이력 조회 API (transport only)
 *
 * `docs/tasks/backend.md` §2-2 를 그대로 반영한다. 계정 단위 기록이라 경로에
 * `{shopId}` 가 없다 — `shopId` 는 좁히기용 query 파라미터이며, 서버는 소유권을
 * 검증하지 않고 토큰의 `ceoId` 로만 필터한다(남의 가게 id 는 빈 목록이 된다).
 */

const ENDPOINT = "/api/ceos";

export const ceoShopAccessHistoryRepository = {
  getList(
    query: CeoShopAccessHistoryListQueryRequest,
    pageRequest: ApiPageRequest,
  ): Promise<ApiResponse<CeoShopAccessHistoryItemResponse[]>> {
    return api.get<CeoShopAccessHistoryItemResponse[]>(`${ENDPOINT}/v1/me/shop-access-histories`, {
      params: { ...query, ...pageRequest },
    });
  },
};
