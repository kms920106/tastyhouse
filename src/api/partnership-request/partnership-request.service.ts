import "server-only";

import type { ApiPageRequest, ApiResponse } from "@/api/shared/types";

import type { PartnershipRequestDetail, PartnershipRequestListItem } from "../../feature/partnership-request/domain";
import type { PartnershipRequestListQueryRequest } from "./partnership-request.dto";
import { partnershipRequestRepository } from "./partnership-request.repository";

export const partnershipRequestService = {
  // 제휴 신청 목록 조회
  // 도메인 반환
  async getList(
    query: PartnershipRequestListQueryRequest,
    pageRequest: ApiPageRequest,
  ): Promise<ApiResponse<PartnershipRequestListItem[]>> {
    const res = await partnershipRequestRepository.getList(query, pageRequest);
    return {
      ...res,
      data: res.data?.map((item) => ({
        id: item.id,
        businessName: item.businessName,
        contactName: item.contactName,
        contactPhone: item.contactPhone,
        status: item.status,
        consultationRequestedAt: item.consultationRequestedAt,
        createdAt: item.createdAt,
      })),
    };
  },

  // 제휴 신청 상세 조회
  // 도메인 반환
  async getDetail(id: number): Promise<ApiResponse<PartnershipRequestDetail>> {
    const res = await partnershipRequestRepository.getDetail(id);
    if (!res.data) return { ...res, data: undefined };
    return {
      ...res,
      data: {
        id: res.data.id,
        businessName: res.data.businessName,
        address: res.data.address,
        addressDetail: res.data.addressDetail,
        contactName: res.data.contactName,
        contactPhone: res.data.contactPhone,
        status: res.data.status,
        consultationRequestedAt: res.data.consultationRequestedAt,
        createdAt: res.data.createdAt,
        updatedAt: res.data.updatedAt,
      },
    };
  },
};
