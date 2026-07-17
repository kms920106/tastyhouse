"use server";

import { revalidatePath } from "next/cache";

import { pointRepository } from "@/api/points/point.repository";
import type { ApiPagination } from "@/api/shared/types";

import type { PointBalance, PointHistoryItem, PointType } from "./domain";
import { POINT_MESSAGE } from "./message";
import {
  type PointDeductFormValues,
  type PointEarnFormValues,
  pointDeductValuesSchema,
  pointEarnValuesSchema,
} from "./schema";

const MEMBERS_PATH = "/dashboard/members";

type ActionResult = {
  success: boolean;
  message?: string;
};

type PointBalanceResult = {
  success: boolean;
  message?: string;
  data?: PointBalance;
};

type PointHistoriesResult = {
  success: boolean;
  message?: string;
  data?: PointHistoryItem[];
  pagination?: ApiPagination;
};

// 포인트 잔액 조회
export async function fetchPointBalanceAction(memberId: number): Promise<PointBalanceResult> {
  const { error, data } = await pointRepository.getBalance(memberId);
  if (error !== undefined) {
    return { success: false, message: error };
  }
  return { success: true, data };
}

// 포인트 이력 조회 (페이징)
export async function fetchPointHistoriesAction(
  memberId: number,
  query: { type?: PointType; page: number; size: number },
): Promise<PointHistoriesResult> {
  const { type, page, size } = query;
  const { error, data, pagination } = await pointRepository.getHistories(memberId, { type }, { page, size });
  if (error !== undefined) {
    return { success: false, message: error };
  }
  return { success: true, data, pagination };
}

// 포인트 수동 적립
export async function earnPointAction(memberId: number, values: PointEarnFormValues): Promise<ActionResult> {
  const parsed = pointEarnValuesSchema.safeParse(values);
  if (!parsed.success) {
    return {
      success: false,
      message: parsed.error.issues[0]?.message ?? POINT_MESSAGE.INVALID_INPUT,
    };
  }

  const { error } = await pointRepository.earn(memberId, {
    amount: parsed.data.amount,
    reason: parsed.data.reason,
  });
  if (error !== undefined) {
    return { success: false, message: error };
  }

  revalidatePath(MEMBERS_PATH);
  return { success: true };
}

// 포인트 수동 차감
export async function deductPointAction(memberId: number, values: PointDeductFormValues): Promise<ActionResult> {
  const parsed = pointDeductValuesSchema.safeParse(values);
  if (!parsed.success) {
    return {
      success: false,
      message: parsed.error.issues[0]?.message ?? POINT_MESSAGE.INVALID_INPUT,
    };
  }

  const { error } = await pointRepository.deduct(memberId, {
    amount: parsed.data.amount,
    reason: parsed.data.reason,
  });
  if (error !== undefined) {
    return { success: false, message: error };
  }

  revalidatePath(MEMBERS_PATH);
  return { success: true };
}
