"use server";

import { revalidatePath } from "next/cache";

import { shopReviewRepository } from "@/api/shop-review/shop-review.repository";

import { SHOP_REVIEW_COPY, SHOP_REVIEW_ERROR_MESSAGE, SHOP_REVIEW_MESSAGE } from "./message";
import {
  type BlindRequestFormValues,
  blindRequestSchema,
  type OwnerReplyFormValues,
  ownerReplySchema,
  type SortTypeFormValues,
  sortTypeSchema,
} from "./schema";

const SHOP_REVIEW_PATH = "/dashboard/shop/reviews";

type ActionResult = {
  success: boolean;
  message?: string;
  id?: number;
};

/**
 * 서버 실패를 사용자 문구로 바꾼다.
 *
 * `errorCode` 표(`SHOP_REVIEW_ERROR_MESSAGE`)에 있으면 그 문구를, 없으면 호출부가 준
 * 기본 문구를 쓴다 — 서버 `message` 를 그대로 노출하지 않는 이유는 `message.ts` 주석 참고.
 */
function toFailure(errorCode: string | undefined, fallback: string): ActionResult {
  return { success: false, message: (errorCode && SHOP_REVIEW_ERROR_MESSAGE[errorCode]) ?? fallback };
}

function invalidInput(message?: string): ActionResult {
  return { success: false, message: message ?? SHOP_REVIEW_MESSAGE.INVALID_INPUT };
}

// ===== 앱 노출 정렬 설정 =====

export async function updateShopReviewSortTypeAction(
  shopId: number,
  values: SortTypeFormValues,
): Promise<ActionResult> {
  const parsed = sortTypeSchema.safeParse(values);
  if (!parsed.success) return invalidInput(parsed.error.issues[0]?.message);

  const { error, errorCode } = await shopReviewRepository.updateSortType(shopId, {
    sortType: parsed.data.sortType,
  });
  if (error !== undefined) return toFailure(errorCode, SHOP_REVIEW_COPY.SORT_TYPE_SAVE_FAILED);

  revalidatePath(SHOP_REVIEW_PATH);
  return { success: true };
}

// ===== 사장님 답변 =====

/**
 * 답변 등록.
 *
 * 클라이언트 검증만 믿지 않고 여기서도 스키마를 다시 태운다 — 서버 액션은 클라이언트를
 * 거치지 않고도 호출될 수 있으므로, 400 을 맞기 전에 같은 규칙으로 막는다.
 * 리뷰당 1건 제약은 서버 `UNIQUE(review_id)` 가 보증하므로 선판정하지 않고 409 를 문구로 갈라 낸다.
 */
export async function createOwnerReplyAction(
  shopId: number,
  reviewId: number,
  values: OwnerReplyFormValues,
): Promise<ActionResult> {
  const parsed = ownerReplySchema.safeParse(values);
  if (!parsed.success) return invalidInput(parsed.error.issues[0]?.message);

  const { data, error, errorCode } = await shopReviewRepository.createOwnerReply(shopId, reviewId, {
    content: parsed.data.content,
  });
  if (error !== undefined) return toFailure(errorCode, SHOP_REVIEW_COPY.OWNER_REPLY_CREATE_FAILED);

  revalidatePath(SHOP_REVIEW_PATH);
  return { success: true, id: data ?? undefined };
}

export async function updateOwnerReplyAction(
  shopId: number,
  reviewId: number,
  values: OwnerReplyFormValues,
): Promise<ActionResult> {
  const parsed = ownerReplySchema.safeParse(values);
  if (!parsed.success) return invalidInput(parsed.error.issues[0]?.message);

  const { error, errorCode } = await shopReviewRepository.updateOwnerReply(shopId, reviewId, {
    content: parsed.data.content,
  });
  if (error !== undefined) return toFailure(errorCode, SHOP_REVIEW_COPY.OWNER_REPLY_UPDATE_FAILED);

  revalidatePath(SHOP_REVIEW_PATH);
  return { success: true };
}

export async function deleteOwnerReplyAction(shopId: number, reviewId: number): Promise<ActionResult> {
  const { error, errorCode } = await shopReviewRepository.deleteOwnerReply(shopId, reviewId);
  if (error !== undefined) return toFailure(errorCode, SHOP_REVIEW_COPY.OWNER_REPLY_DELETE_FAILED);

  revalidatePath(SHOP_REVIEW_PATH);
  return { success: true };
}

// ===== 게시중단 요청 =====

/**
 * 게시중단 요청 등록.
 *
 * 상세 사유가 빈 문자열이면 보내지 않는다 — 서버가 `ETC` 필수 판정을 할 때 빈 문자열과
 * 미전송을 다르게 볼 여지를 남기지 않기 위함이다.
 */
export async function createBlindRequestAction(
  shopId: number,
  reviewId: number,
  values: BlindRequestFormValues,
): Promise<ActionResult> {
  const parsed = blindRequestSchema.safeParse(values);
  if (!parsed.success) return invalidInput(parsed.error.issues[0]?.message);

  const detailReason = parsed.data.detailReason;
  const { data, error, errorCode } = await shopReviewRepository.createBlindRequest(shopId, reviewId, {
    reason: parsed.data.reason,
    ...(detailReason && detailReason.length > 0 ? { detailReason } : {}),
  });
  if (error !== undefined) return toFailure(errorCode, SHOP_REVIEW_COPY.BLIND_REQUEST_FAILED);

  revalidatePath(SHOP_REVIEW_PATH);
  return { success: true, id: data ?? undefined };
}

/**
 * 게시중단 요청 취소.
 *
 * 취소 가능 조건(PENDING 만)은 서버 애그리거트의 불변식이라 여기서 선판정하지 않고,
 * 409 `REVIEW_BLIND_REQUEST_NOT_PENDING` 을 전용 문구로 갈라 낸다.
 */
export async function cancelBlindRequestAction(shopId: number, requestId: number): Promise<ActionResult> {
  const { error, errorCode } = await shopReviewRepository.cancelBlindRequest(shopId, requestId);
  if (error !== undefined) return toFailure(errorCode, SHOP_REVIEW_COPY.BLIND_CANCEL_FAILED);

  revalidatePath(SHOP_REVIEW_PATH);
  return { success: true };
}
