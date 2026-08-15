import "server-only";

import type { ApiResponse } from "@/api/shared/types";
import type { ShopNoticeItem } from "@/feature/shop-notice/domain";

import type { ShopNoticeResponse } from "./shop-notice.dto";
import { shopNoticeRepository } from "./shop-notice.repository";

// 날짜 문자열은 그대로 둔다 — 렌더 시점에 포맷한다.
function toNoticeItem(item: ShopNoticeResponse): ShopNoticeItem {
  return {
    id: item.id,
    content: item.content,
    // 이미지가 없으면 빈 배열로 내려오는 것이 서버 계약이다(`docs/tasks/backend.md` 3-1).
    imageUrls: item.imageUrls,
    exposed: item.exposed,
    hidden: item.hidden,
    createdAt: item.createdAt,
    updatedAt: item.updatedAt,
  };
}

export const shopNoticeService = {
  async getNotices(shopId: number): Promise<ApiResponse<ShopNoticeItem[]>> {
    const response = await shopNoticeRepository.getList(shopId);
    if (response.error !== undefined || !response.data) return response as ApiResponse<ShopNoticeItem[]>;

    return { ...response, data: response.data.map(toNoticeItem) };
  },
};
