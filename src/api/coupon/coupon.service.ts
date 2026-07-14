import "server-only";

import type { ApiPageRequest, ApiResponse } from "@/api/shared/types";

import type { CouponDetail, CouponListItem, MemberCouponItem } from "../../feature/coupon/domain";
import type { CouponListQueryRequest } from "./coupon.dto";
import { couponRepository } from "./coupon.repository";

export const couponService = {
  // 쿠폰 목록 조회
  // 도메인 반환
  async getCoupons(query: CouponListQueryRequest, pageRequest: ApiPageRequest): Promise<ApiResponse<CouponListItem[]>> {
    const res = await couponRepository.getList(query, pageRequest);
    return {
      ...res,
      data: res.data?.map((item) => ({
        id: item.id,
        name: item.name,
        discountType: item.discountType,
        discountAmount: item.discountAmount,
        maxDiscountAmount: item.maxDiscountAmount,
        minOrderAmount: item.minOrderAmount,
        maxDiscountCount: item.maxDiscountCount,
        issueStartAt: item.issueStartAt,
        issueEndAt: item.issueEndAt,
        useStartAt: item.useStartAt,
        useEndAt: item.useEndAt,
        visible: item.visible,
      })),
    };
  },

  // 쿠폰 상세 조회
  // 도메인 반환
  async getCoupon(id: number): Promise<ApiResponse<CouponDetail>> {
    const res = await couponRepository.getDetail(id);
    if (!res.data) return { ...res, data: undefined };
    return {
      ...res,
      data: {
        id: res.data.id,
        name: res.data.name,
        description: res.data.description,
        discountType: res.data.discountType,
        discountAmount: res.data.discountAmount,
        maxDiscountAmount: res.data.maxDiscountAmount,
        minOrderAmount: res.data.minOrderAmount,
        maxDiscountCount: res.data.maxDiscountCount,
        issueStartAt: res.data.issueStartAt,
        issueEndAt: res.data.issueEndAt,
        useStartAt: res.data.useStartAt,
        useEndAt: res.data.useEndAt,
        visible: res.data.visible,
        createdAt: res.data.createdAt,
        updatedAt: res.data.updatedAt,
      },
    };
  },

  // 쿠폰 발급 현황 조회
  // 도메인 반환
  async getCouponIssues(couponId: number, pageRequest: ApiPageRequest): Promise<ApiResponse<MemberCouponItem[]>> {
    const res = await couponRepository.getIssues(couponId, pageRequest);
    return {
      ...res,
      data: res.data?.map((item) => ({
        id: item.id,
        memberId: item.memberId,
        used: item.used,
        usedAt: item.usedAt,
        expiredAt: item.expiredAt,
        issuedAt: item.issuedAt,
      })),
    };
  },
};
