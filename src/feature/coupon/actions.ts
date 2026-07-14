"use server";

import { revalidatePath } from "next/cache";

import { couponRepository } from "@/api/coupon/coupon.repository";
import { couponService } from "@/api/coupon/coupon.service";
import type { ApiPagination } from "@/api/shared/types";
import type { CouponDetail, MemberCouponItem } from "@/feature/coupon/domain";

import { COUPON_MESSAGE } from "./message";
import { type CouponFormValues, type CouponIssueFormValues, couponFormSchema, couponIssueSchema } from "./schema";

const COUPONS_PATH = "/dashboard/coupons";

type ActionResult = {
  success: boolean;
  message?: string;
  id?: number;
};

type CouponDetailResult = {
  success: boolean;
  message?: string;
  data?: CouponDetail;
};

type CouponIssuesResult = {
  success: boolean;
  message?: string;
  data?: MemberCouponItem[];
  pagination?: ApiPagination;
};

/** "YYYY-MM-DDTHH:mm" (datetime-local) -> "YYYY-MM-DDTHH:mm:ss" (LocalDateTime) */
function toLocalDateTime(value: string): string {
  return value.length === 16 ? `${value}:00` : value;
}

function toRequestBody(values: CouponFormValues) {
  return {
    name: values.name,
    description: values.description,
    discountType: values.discountType,
    discountAmount: values.discountAmount,
    maxDiscountAmount: values.maxDiscountAmount,
    minOrderAmount: values.minOrderAmount,
    maxDiscountCount: values.maxDiscountCount,
    issueStartAt: toLocalDateTime(values.issueStartAt),
    issueEndAt: toLocalDateTime(values.issueEndAt),
    useStartAt: toLocalDateTime(values.useStartAt),
    useEndAt: toLocalDateTime(values.useEndAt),
    visible: values.visible,
  };
}

// 쿠폰 등록
export async function createCouponAction(values: CouponFormValues): Promise<ActionResult> {
  const parsed = couponFormSchema.safeParse(values);
  if (!parsed.success) {
    return {
      success: false,
      message: parsed.error.issues[0]?.message ?? COUPON_MESSAGE.INVALID_INPUT,
    };
  }

  const { error, data } = await couponRepository.create(toRequestBody(parsed.data));
  if (error !== undefined) {
    return { success: false, message: error };
  }

  revalidatePath(COUPONS_PATH);
  return { success: true, id: data };
}

// 쿠폰 상세 조회
export async function fetchCouponAction(id: number): Promise<CouponDetailResult> {
  const { error, data } = await couponService.getCoupon(id);
  if (error !== undefined) {
    return { success: false, message: error };
  }
  return { success: true, data };
}

// 쿠폰 수정
export async function updateCouponAction(id: number, values: CouponFormValues): Promise<ActionResult> {
  const parsed = couponFormSchema.safeParse(values);
  if (!parsed.success) {
    return {
      success: false,
      message: parsed.error.issues[0]?.message ?? COUPON_MESSAGE.INVALID_INPUT,
    };
  }

  const { error } = await couponRepository.update(id, toRequestBody(parsed.data));
  if (error !== undefined) {
    return { success: false, message: error };
  }

  revalidatePath(COUPONS_PATH);
  return { success: true };
}

// 쿠폰 삭제 (Soft Delete)
export async function deleteCouponAction(id: number): Promise<ActionResult> {
  const { error } = await couponRepository.remove(id);
  if (error !== undefined) {
    return { success: false, message: error };
  }

  revalidatePath(COUPONS_PATH);
  return { success: true };
}

// 쿠폰 회원 발급
export async function issueCouponAction(couponId: number, values: CouponIssueFormValues): Promise<ActionResult> {
  const parsed = couponIssueSchema.safeParse(values);
  if (!parsed.success) {
    return {
      success: false,
      message: parsed.error.issues[0]?.message ?? COUPON_MESSAGE.INVALID_INPUT,
    };
  }

  // 409 COUPON_ALREADY_ISSUED / 404 COUPON_NOT_FOUND 는 client 가 백엔드 message 를 보존하므로 그대로 노출한다.
  const { error, data } = await couponRepository.issue(couponId, parsed.data);
  if (error !== undefined) {
    return { success: false, message: error };
  }

  // 발급은 쿠폰 목록 표시 데이터에 영향을 주지 않고, 발급 현황은 시트 내부에서 재조회하므로
  // revalidatePath(COUPONS_PATH) 를 호출하지 않는다.
  return { success: true, id: data };
}

// 쿠폰 발급 현황 조회
export async function fetchCouponIssuesAction(
  couponId: number,
  page: number,
  size: number,
): Promise<CouponIssuesResult> {
  const { error, data, pagination } = await couponService.getCouponIssues(couponId, { page, size });
  if (error !== undefined) {
    return { success: false, message: error };
  }
  return { success: true, data, pagination };
}
