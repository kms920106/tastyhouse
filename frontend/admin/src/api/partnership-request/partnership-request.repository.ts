import "server-only";

import { api } from "@/api/shared/client";
import type { ApiPageRequest, ApiResponse } from "@/api/shared/types";

import type {
  PartnershipRequestDetailResponse,
  PartnershipRequestListItemResponse,
  PartnershipRequestListQueryRequest,
  PartnershipRequestStatusUpdateRequest,
} from "./partnership-request.dto";

/**
 * 제휴 신청 관리자 API
 */

const ENDPOINT = "/api/partnership-requests";

export const partnershipRequestRepository = {
  // 제휴 신청 목록 조회
  getList(
    query: PartnershipRequestListQueryRequest,
    pageRequest: ApiPageRequest,
  ): Promise<ApiResponse<PartnershipRequestListItemResponse[]>> {
    return api.get<PartnershipRequestListItemResponse[]>(`${ENDPOINT}/v1`, {
      params: { ...query, ...pageRequest },
    });
  },

  // 제휴 신청 상세 조회
  getDetail(id: number): Promise<ApiResponse<PartnershipRequestDetailResponse>> {
    return api.get<PartnershipRequestDetailResponse>(`${ENDPOINT}/v1/${id}`);
  },

  // 제휴 신청 처리 상태 변경
  updateStatus(id: number, body: PartnershipRequestStatusUpdateRequest): Promise<ApiResponse<null>> {
    return api.patch<null>(`${ENDPOINT}/v1/${id}/status`, body);
  },

  // 제휴 신청 삭제 (Soft Delete)
  remove(id: number): Promise<ApiResponse<null>> {
    return api.delete<null>(`${ENDPOINT}/v1/${id}`);
  },
};
