import "server-only";

import type { ApiPageRequest, ApiResponse } from "@/api/shared/types";
import type { CeoLoginHistoryItem } from "@/feature/ceo/domain";

import type { CeoLoginHistoryItemResponse, CeoLoginHistoryListQueryRequest } from "./ceo-login-history.dto";
import { ceoLoginHistoryRepository } from "./ceo-login-history.repository";

// 날짜 문자열은 그대로 둔다 — 렌더 시점에 formatDateTime 으로 포맷한다.
function toLoginHistoryItem(item: CeoLoginHistoryItemResponse): CeoLoginHistoryItem {
  return {
    id: item.id,
    result: item.result,
    resultName: item.resultName,
    failureReason: item.failureReason,
    failureReasonName: item.failureReasonName,
    ipAddress: item.ipAddress,
    userAgent: item.userAgent,
    loggedInAt: item.loggedInAt,
  };
}

export const ceoLoginHistoryService = {
  // 목록 조회 — pagination 은 래퍼 그대로 넘겨 페이지네이션이 totalPages 를 쓰게 한다.
  async getLoginHistories(
    query: CeoLoginHistoryListQueryRequest,
    pageRequest: ApiPageRequest,
  ): Promise<ApiResponse<CeoLoginHistoryItem[]>> {
    const result = await ceoLoginHistoryRepository.getList(query, pageRequest);
    if (result.error || !result.data) return { ...result, data: undefined };

    return { ...result, data: result.data.map(toLoginHistoryItem) };
  },
};
