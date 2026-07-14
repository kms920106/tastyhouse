import "server-only";

import { api } from "@/api/shared/client";
import type { ApiPageRequest, ApiResponse } from "@/api/shared/types";

import type {
  CouponCreateRequest,
  CouponDetailResponse,
  CouponIssueRequest,
  CouponListItemResponse,
  CouponListQueryRequest,
  CouponUpdateRequest,
  MemberCouponAdminItemResponse,
} from "./coupon.dto";

/**
 * 쿠폰 관리자 API
 */

const ENDPOINT = "/api/coupons";

export const couponRepository = {
  // 쿠폰 목록 조회
  getList(query: CouponListQueryRequest, pageRequest: ApiPageRequest): Promise<ApiResponse<CouponListItemResponse[]>> {
    return api.get<CouponListItemResponse[]>(`${ENDPOINT}/v1`, {
      params: { ...query, ...pageRequest },
    });
  },

  // 쿠폰 등록
  create(body: CouponCreateRequest): Promise<ApiResponse<number>> {
    return api.post<number>(`${ENDPOINT}/v1`, body);
  },

  // 쿠폰 상세 조회
  getDetail(id: number): Promise<ApiResponse<CouponDetailResponse>> {
    return api.get<CouponDetailResponse>(`${ENDPOINT}/v1/${id}`);
  },

  // 쿠폰 수정
  update(id: number, body: CouponUpdateRequest): Promise<ApiResponse<null>> {
    return api.put<null>(`${ENDPOINT}/v1/${id}`, body);
  },

  // 쿠폰 삭제 (Soft Delete)
  remove(id: number): Promise<ApiResponse<null>> {
    return api.delete<null>(`${ENDPOINT}/v1/${id}`);
  },

  // 쿠폰 회원 발급
  issue(couponId: number, body: CouponIssueRequest): Promise<ApiResponse<number>> {
    return api.post<number>(`${ENDPOINT}/v1/${couponId}/issues`, body);
  },

  // 쿠폰 발급 현황 조회
  getIssues(couponId: number, pageRequest: ApiPageRequest): Promise<ApiResponse<MemberCouponAdminItemResponse[]>> {
    return api.get<MemberCouponAdminItemResponse[]>(`${ENDPOINT}/v1/${couponId}/issues`, {
      params: { ...pageRequest },
    });
  },
};
