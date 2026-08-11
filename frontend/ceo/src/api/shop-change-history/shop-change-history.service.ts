import "server-only";

import type { ApiPageRequest, ApiResponse } from "@/api/shared/types";
import type { ShopChangeCategoryOption, ShopChangeHistoryItem } from "@/feature/shop/domain";

import type {
  ShopChangeCategoryResponse,
  ShopChangeHistoryItemResponse,
  ShopChangeHistoryListQueryRequest,
} from "./shop-change-history.dto";
import { shopChangeHistoryRepository } from "./shop-change-history.repository";

// 날짜 문자열은 그대로 둔다 — 렌더 시점에 formatDateTime 으로 포맷한다.
function toChangeHistoryItem(item: ShopChangeHistoryItemResponse): ShopChangeHistoryItem {
  return {
    id: item.id,
    category: item.category,
    categoryName: item.categoryName,
    changeType: item.changeType,
    changeTypeName: item.changeTypeName,
    actionType: item.actionType,
    actionTypeName: item.actionTypeName,
    previousValue: item.previousValue,
    newValue: item.newValue,
    changedAt: item.changedAt,
  };
}

function toCategoryOption(item: ShopChangeCategoryResponse): ShopChangeCategoryOption {
  return {
    code: item.code,
    name: item.name,
    changeTypes: item.changeTypes.map((changeType) => ({ code: changeType.code, name: changeType.name })),
  };
}

export const shopChangeHistoryService = {
  // 목록 조회 — pagination 은 래퍼 그대로 넘겨 페이지네이션이 totalPages 를 쓰게 한다.
  async getList(
    shopId: number,
    query: ShopChangeHistoryListQueryRequest,
    pageRequest: ApiPageRequest,
  ): Promise<ApiResponse<ShopChangeHistoryItem[]>> {
    const result = await shopChangeHistoryRepository.getList(shopId, query, pageRequest);
    if (result.error || !result.data) return { ...result, data: undefined };

    return { ...result, data: result.data.map(toChangeHistoryItem) };
  },

  async getChangeTypes(): Promise<ApiResponse<ShopChangeCategoryOption[]>> {
    const result = await shopChangeHistoryRepository.getChangeTypes();
    if (result.error || !result.data) return { ...result, data: undefined };

    return { ...result, data: result.data.map(toCategoryOption) };
  },
};
