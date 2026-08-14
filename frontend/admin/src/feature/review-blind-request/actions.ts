"use server";

import { revalidatePath } from "next/cache";

import { reviewBlindRequestRepository } from "@/api/review-blind-request/review-blind-request.repository";

import { REVIEW_BLIND_REQUEST_ERROR_MESSAGE, REVIEW_BLIND_REQUEST_MESSAGE } from "./message";
import { type RejectFormValues, rejectSchema } from "./schema";

const REVIEW_BLIND_REQUESTS_PATH = "/dashboard/review-blind-requests";

type ActionResult = {
  success: boolean;
  message?: string;
};

/**
 * 서버 errorCode 를 사용자 문구로 승격한다.
 * 매핑에 없는 코드는 백엔드가 내려준 한국어 메시지를 그대로 쓰고, 그마저 없으면 폴백을 쓴다.
 */
function toErrorMessage(errorCode: string | undefined, serverMessage: string | undefined, fallback: string): string {
  if (errorCode && REVIEW_BLIND_REQUEST_ERROR_MESSAGE[errorCode]) {
    return REVIEW_BLIND_REQUEST_ERROR_MESSAGE[errorCode];
  }
  return serverMessage ?? fallback;
}

// 게시중단 요청 승인 — 리뷰가 고객 화면에서 즉시 숨겨진다.
export async function approveBlindRequestAction(id: number): Promise<ActionResult> {
  const { error, errorCode, message } = await reviewBlindRequestRepository.approve(id);

  // 이미 처리된 요청(다른 관리자 선처리)도 목록을 갱신해 최신 상태를 보여준다.
  revalidatePath(REVIEW_BLIND_REQUESTS_PATH);

  if (error !== undefined) {
    return {
      success: false,
      message: toErrorMessage(errorCode, message, REVIEW_BLIND_REQUEST_MESSAGE.APPROVE_FAILED),
    };
  }

  return { success: true };
}

// 게시중단 요청 반려 — 리뷰는 노출 상태를 유지한다.
export async function rejectBlindRequestAction(id: number, values: RejectFormValues): Promise<ActionResult> {
  const parsed = rejectSchema.safeParse(values);
  if (!parsed.success) {
    return { success: false, message: parsed.error.issues[0]?.message ?? REVIEW_BLIND_REQUEST_MESSAGE.INVALID_INPUT };
  }

  const { error, errorCode, message } = await reviewBlindRequestRepository.reject(id, parsed.data);

  revalidatePath(REVIEW_BLIND_REQUESTS_PATH);

  if (error !== undefined) {
    return {
      success: false,
      message: toErrorMessage(errorCode, message, REVIEW_BLIND_REQUEST_MESSAGE.REJECT_FAILED),
    };
  }

  return { success: true };
}
