import "server-only";

import type { ApiPageRequest, ApiResponse } from "@/api/shared/types";
import type { CeoShopAccessHistoryItem } from "@/feature/ceo/domain";

import type {
  CeoShopAccessHistoryItemResponse,
  CeoShopAccessHistoryListQueryRequest,
} from "./ceo-shop-access-history.dto";
import { ceoShopAccessHistoryRepository } from "./ceo-shop-access-history.repository";

// 날짜 문자열은 그대로 둔다 — 렌더 시점에 formatDateTime 으로 포맷한다.
function toShopAccessHistoryItem(item: CeoShopAccessHistoryItemResponse): CeoShopAccessHistoryItem {
  return {
    id: item.id,
    shopId: item.shopId,
    shopName: item.shopName,
    actionType: item.actionType,
    actionTypeName: item.actionTypeName,
    occurredAt: item.occurredAt,
  };
}

export const ceoShopAccessHistoryService = {
  // 목록 조회 — pagination 은 래퍼 그대로 넘겨 페이지네이션이 totalPages 를 쓰게 한다.
  async getShopAccessHistories(
    query: CeoShopAccessHistoryListQueryRequest,
    pageRequest: ApiPageRequest,
  ): Promise<ApiResponse<CeoShopAccessHistoryItem[]>> {
    const result = await ceoShopAccessHistoryRepository.getList(query, pageRequest);
    if (result.error || !result.data) return { ...result, data: undefined };

    return { ...result, data: result.data.map(toShopAccessHistoryItem) };
  },
};
