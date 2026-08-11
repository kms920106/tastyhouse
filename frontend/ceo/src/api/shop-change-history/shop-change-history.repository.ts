import "server-only";

import { api } from "@/api/shared/client";
import type { ApiPageRequest, ApiResponse } from "@/api/shared/types";

import type {
  ShopChangeCategoryResponse,
  ShopChangeHistoryItemResponse,
  ShopChangeHistoryListQueryRequest,
} from "./shop-change-history.dto";

/**
 * 점주용 가게 변경이력 조회 API (transport only)
 *
 * 하위 리소스 경로 규칙은 리소스마다 다르므로(`src/api/AGENTS.md`) 일반화하지 않고
 * `docs/tasks/backend.md` 3-1·3-2 를 그대로 반영한다.
 * - 목록은 부모 `{shopId}` 경로를 유지한다 (`/v1/{shopId}/change-histories`)
 * - 분류 카탈로그는 가게에 종속되지 않는 정적 데이터라 `{shopId}` 가 없다
 *   (`/v1/change-history-types`)
 */

const ENDPOINT = "/api/shops";

export const shopChangeHistoryRepository = {
  getList(
    shopId: number,
    query: ShopChangeHistoryListQueryRequest,
    pageRequest: ApiPageRequest,
  ): Promise<ApiResponse<ShopChangeHistoryItemResponse[]>> {
    return api.get<ShopChangeHistoryItemResponse[]>(`${ENDPOINT}/v1/${shopId}/change-histories`, {
      params: { ...query, ...pageRequest },
    });
  },

  // 가게에 종속되지 않는 정적 카탈로그 — shopId 를 받지 않는다.
  getChangeTypes(): Promise<ApiResponse<ShopChangeCategoryResponse[]>> {
    return api.get<ShopChangeCategoryResponse[]>(`${ENDPOINT}/v1/change-history-types`);
  },
};
