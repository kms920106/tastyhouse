import "server-only";

import type { ApiPageRequest, ApiResponse } from "@/api/shared/types";
import type {
  ShopRequestComment,
  ShopRequestDetail,
  ShopRequestListItem,
  ShopRequestStatusOption,
  ShopRequestTypeOption,
} from "@/feature/shop/domain";

import type {
  ShopRequestCommentResponse,
  ShopRequestDetailResponse,
  ShopRequestListItemResponse,
  ShopRequestListQueryRequest,
  ShopRequestTypeCatalogResponse,
} from "./shop-request.dto";
import { shopRequestRepository } from "./shop-request.repository";

/** 서버 카탈로그가 내려주는 요청 유형·상태 옵션 묶음 */
export interface ShopRequestCatalog {
  requestTypes: ShopRequestTypeOption[];
  statuses: ShopRequestStatusOption[];
}

// 날짜 문자열은 그대로 둔다 — 렌더 시점에 formatDateTime 으로 포맷한다.
function toListItem(item: ShopRequestListItemResponse): ShopRequestListItem {
  return {
    requestId: item.requestId,
    requestType: item.requestType,
    requestTypeDescription: item.requestTypeDescription,
    summary: item.summary,
    status: item.status,
    statusDescription: item.statusDescription,
    rejectReason: item.rejectReason,
    contractAmending: item.contractAmending,
    hasAttachment: item.hasAttachment,
    commentCount: item.commentCount,
    requestedAt: item.requestedAt,
    processedAt: item.processedAt,
  };
}

function toDetail(item: ShopRequestDetailResponse): ShopRequestDetail {
  return {
    ...toListItem(item),
    attachmentLabel: item.attachmentLabel,
    attachmentUrl: item.attachmentUrl,
    imageChange: item.imageChange
      ? {
          imageType: item.imageChange.imageType,
          imageTypeDescription: item.imageChange.imageTypeDescription,
          imageUrl: item.imageChange.imageUrl,
        }
      : null,
    deliveryAreaAdjustment: item.deliveryAreaAdjustment
      ? {
          counterpartShopName: item.deliveryAreaAdjustment.counterpartShopName,
          counterpartBusinessNumber: item.deliveryAreaAdjustment.counterpartBusinessNumber,
          franchiseName: item.deliveryAreaAdjustment.franchiseName,
          reason: item.deliveryAreaAdjustment.reason,
          consentFileUrl: item.deliveryAreaAdjustment.consentFileUrl,
        }
      : null,
  };
}

function toComment(item: ShopRequestCommentResponse): ShopRequestComment {
  return {
    commentId: item.commentId,
    authorType: item.authorType,
    authorTypeDescription: item.authorTypeDescription,
    content: item.content,
    createdAt: item.createdAt,
  };
}

function toCatalog(item: ShopRequestTypeCatalogResponse): ShopRequestCatalog {
  return {
    requestTypes: item.requestTypes.map((requestType) => ({
      code: requestType.code,
      description: requestType.description,
      contractAmending: requestType.contractAmending,
    })),
    statuses: item.statuses.map((status) => ({ code: status.code, description: status.description })),
  };
}

export const shopRequestService = {
  // 목록 조회 — pagination 은 래퍼 그대로 넘겨 페이지네이션이 totalPages 를 쓰게 한다.
  async getList(
    shopId: number,
    query: ShopRequestListQueryRequest,
    pageRequest: ApiPageRequest,
  ): Promise<ApiResponse<ShopRequestListItem[]>> {
    const result = await shopRequestRepository.getList(shopId, query, pageRequest);
    if (result.error || !result.data) return { ...result, data: undefined };

    return { ...result, data: result.data.map(toListItem) };
  },

  async getDetail(shopId: number, requestId: number): Promise<ApiResponse<ShopRequestDetail>> {
    const result = await shopRequestRepository.getDetail(shopId, requestId);
    if (result.error || !result.data) return { ...result, data: undefined };

    return { ...result, data: toDetail(result.data) };
  },

  async getComments(shopId: number, requestId: number): Promise<ApiResponse<ShopRequestComment[]>> {
    const result = await shopRequestRepository.getComments(shopId, requestId);
    if (result.error || !result.data) return { ...result, data: undefined };

    return { ...result, data: result.data.map(toComment) };
  },

  async getRequestTypes(): Promise<ApiResponse<ShopRequestCatalog>> {
    const result = await shopRequestRepository.getRequestTypes();
    if (result.error || !result.data) return { ...result, data: undefined };

    return { ...result, data: toCatalog(result.data) };
  },
};
