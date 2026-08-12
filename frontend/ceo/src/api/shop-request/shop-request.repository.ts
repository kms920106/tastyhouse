import "server-only";

import { api } from "@/api/shared/client";
import type { ApiPageRequest, ApiResponse } from "@/api/shared/types";

import type {
  ShopRequestCommentCreateRequest,
  ShopRequestCommentResponse,
  ShopRequestDetailResponse,
  ShopRequestListItemResponse,
  ShopRequestListQueryRequest,
  ShopRequestTypeCatalogResponse,
} from "./shop-request.dto";

/**
 * 점주용 가게 요청처리 현황 API (transport only)
 *
 * 하위 리소스 경로 규칙은 리소스마다 다르므로(`src/api/AGENTS.md`) 일반화하지 않고
 * `docs/tasks/backend.md` 4-1 ~ 4-6 을 그대로 반영한다.
 * - 목록·상세·취소·문의는 전부 부모 `{shopId}` 경로를 유지한다 (`/v1/{shopId}/requests/...`)
 * - `requestId` 는 인덱스 행 ID 로, 상세·취소·문의의 유일한 식별자다
 */

const ENDPOINT = "/api/shops";

// 카탈로그는 가게에 종속되지 않는 정적 목록이라 다른 함수와 달리 shopId 를 받지 않는다.
const CATALOG_PATH = `${ENDPOINT}/v1/request-types`;

export const shopRequestRepository = {
  getList(
    shopId: number,
    query: ShopRequestListQueryRequest,
    pageRequest: ApiPageRequest,
  ): Promise<ApiResponse<ShopRequestListItemResponse[]>> {
    return api.get<ShopRequestListItemResponse[]>(`${ENDPOINT}/v1/${shopId}/requests`, {
      params: { ...query, ...pageRequest },
    });
  },

  getDetail(shopId: number, requestId: number): Promise<ApiResponse<ShopRequestDetailResponse>> {
    return api.get<ShopRequestDetailResponse>(`${ENDPOINT}/v1/${shopId}/requests/${requestId}`);
  },

  // 본문 없는 PATCH — 상태 전이만 지시하고 응답은 ApiResponse<Void> 다.
  cancel(shopId: number, requestId: number): Promise<ApiResponse<void>> {
    return api.patch<void>(`${ENDPOINT}/v1/${shopId}/requests/${requestId}/cancel`);
  },

  // 문의 스레드는 대화 순서(created_at ASC)로 내려오며 페이징이 없다.
  getComments(shopId: number, requestId: number): Promise<ApiResponse<ShopRequestCommentResponse[]>> {
    return api.get<ShopRequestCommentResponse[]>(`${ENDPOINT}/v1/${shopId}/requests/${requestId}/comments`);
  },

  // 등록 POST 는 생성된 PK 만 반환하는 규칙을 따른다.
  createComment(
    shopId: number,
    requestId: number,
    body: ShopRequestCommentCreateRequest,
  ): Promise<ApiResponse<number>> {
    return api.post<number>(`${ENDPOINT}/v1/${shopId}/requests/${requestId}/comments`, body);
  },

  getRequestTypes(): Promise<ApiResponse<ShopRequestTypeCatalogResponse>> {
    return api.get<ShopRequestTypeCatalogResponse>(CATALOG_PATH);
  },
};
