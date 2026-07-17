import "server-only";

import { api } from "@/api/shared/client";
import type { ApiPageRequest, ApiResponse } from "@/api/shared/types";

import type {
  MemberDetailResponse,
  MemberListItemResponse,
  MemberListQueryRequest,
  MemberWithdrawalRequest,
} from "./member.dto";

/**
 * 회원 관리자 API
 */

const ENDPOINT = "/api/members";

export const memberRepository = {
  // 회원 목록 조회
  getList(query: MemberListQueryRequest, pageRequest: ApiPageRequest): Promise<ApiResponse<MemberListItemResponse[]>> {
    return api.get<MemberListItemResponse[]>(`${ENDPOINT}/v1`, {
      params: { ...query, ...pageRequest },
    });
  },

  // 회원 상세 조회
  getDetail(id: number): Promise<ApiResponse<MemberDetailResponse>> {
    return api.get<MemberDetailResponse>(`${ENDPOINT}/v1/${id}`);
  },

  // 회원 정지 (ACTIVE -> SUSPENDED)
  suspend(id: number): Promise<ApiResponse<null>> {
    return api.patch<null>(`${ENDPOINT}/v1/${id}/suspend`);
  },

  // 회원 정지 해제 (SUSPENDED -> ACTIVE)
  activate(id: number): Promise<ApiResponse<null>> {
    return api.patch<null>(`${ENDPOINT}/v1/${id}/activate`);
  },

  // 회원 강제 탈퇴 (-> DELETED)
  withdraw(id: number, body: MemberWithdrawalRequest): Promise<ApiResponse<null>> {
    return api.post<null>(`${ENDPOINT}/v1/${id}/withdrawal`, body);
  },
};
