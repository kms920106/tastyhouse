import "server-only";

import { api } from "@/api/shared/client";
import type { ApiPageRequest, ApiResponse } from "@/api/shared/types";

import type {
  OrderDetailResponse,
  OrderListItemResponse,
  OrderListQueryRequest,
  OrderStatusUpdateRequest,
} from "./order.dto";

/**
 * 주문 관리자 API
 */

const ENDPOINT = "/api/orders";

export const orderRepository = {
  // 주문 목록 조회
  getList(query: OrderListQueryRequest, pageRequest: ApiPageRequest): Promise<ApiResponse<OrderListItemResponse[]>> {
    return api.get<OrderListItemResponse[]>(`${ENDPOINT}/v1`, {
      params: { ...query, ...pageRequest },
    });
  },

  // 주문 상세 조회
  getDetail(id: number): Promise<ApiResponse<OrderDetailResponse>> {
    return api.get<OrderDetailResponse>(`${ENDPOINT}/v1/${id}`);
  },

  // 주문 상태 변경
  updateStatus(id: number, body: OrderStatusUpdateRequest): Promise<ApiResponse<null>> {
    return api.patch<null>(`${ENDPOINT}/v1/${id}/status`, body);
  },

  // 주문 삭제 (Soft Delete)
  remove(id: number): Promise<ApiResponse<null>> {
    return api.delete<null>(`${ENDPOINT}/v1/${id}`);
  },
};
