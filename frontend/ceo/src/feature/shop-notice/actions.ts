"use server";

import { revalidatePath } from "next/cache";

import { shopNoticeRepository } from "@/api/shop-notice/shop-notice.repository";
import { shopNoticeService } from "@/api/shop-notice/shop-notice.service";

import { NOTICE_IMAGE_MAX_COUNT } from "./constants";
import type { ShopNoticeItem } from "./domain";
import { SHOP_NOTICE_COPY, SHOP_NOTICE_ERROR_MESSAGE, SHOP_NOTICE_MESSAGE } from "./message";
import { noticeSchema } from "./schema";

const SHOP_PATH = "/dashboard/shop";

type ActionResult<T = never> = {
  success: boolean;
  message?: string;
  data?: T;
};

/**
 * 서버 실패를 사용자 문구로 바꾼다.
 *
 * `errorCode` 표(`SHOP_NOTICE_ERROR_MESSAGE`)에 있으면 그 문구를, 없으면 호출부가 준
 * 기본 문구를 쓴다 — 서버 `message` 를 그대로 노출하지 않는 이유는 `message.ts` 주석 참고.
 */
function toFailure(errorCode: string | undefined, fallback: string): ActionResult<never> {
  return { success: false, message: (errorCode && SHOP_NOTICE_ERROR_MESSAGE[errorCode]) ?? fallback };
}

function invalidInput(message?: string): ActionResult<never> {
  return { success: false, message: message ?? SHOP_NOTICE_MESSAGE.INVALID_INPUT };
}

/**
 * FormData 의 `content` 를 스키마로 다시 검증한다.
 *
 * 클라이언트 검증만 믿지 않는다 — 서버 액션은 클라이언트를 거치지 않고도 호출될 수 있으므로,
 * 400 을 맞기 전에 같은 규칙으로 막는다. 이미지 장수도 같은 이유로 여기서 한 번 더 센다.
 */
function validateNoticeFormData(formData: FormData): { message: string } | null {
  const parsed = noticeSchema.safeParse({ content: formData.get("content") ?? "" });
  if (!parsed.success) return { message: parsed.error.issues[0]?.message ?? SHOP_NOTICE_MESSAGE.INVALID_INPUT };

  const files = formData.getAll("files").filter((entry) => entry instanceof File && entry.size > 0);
  if (files.length > NOTICE_IMAGE_MAX_COUNT) return { message: SHOP_NOTICE_MESSAGE.IMAGE_MAX_REACHED };

  return null;
}

export async function fetchNoticesAction(shopId: number): Promise<ActionResult<ShopNoticeItem[]>> {
  const { data, error, errorCode } = await shopNoticeService.getNotices(shopId);
  if (error !== undefined) return toFailure(errorCode, SHOP_NOTICE_COPY.LOAD_FAILED);

  return { success: true, data: data ?? [] };
}

export async function createNoticeAction(shopId: number, formData: FormData): Promise<ActionResult<number>> {
  const invalid = validateNoticeFormData(formData);
  if (invalid) return invalidInput(invalid.message);

  const { data, error, errorCode } = await shopNoticeRepository.create(shopId, formData);
  if (error !== undefined) return toFailure(errorCode, SHOP_NOTICE_COPY.CREATE_FAILED);

  revalidatePath(SHOP_PATH);
  return { success: true, data: data ?? undefined };
}

export async function updateNoticeAction(
  shopId: number,
  noticeId: number,
  formData: FormData,
): Promise<ActionResult<never>> {
  const invalid = validateNoticeFormData(formData);
  if (invalid) return invalidInput(invalid.message);

  const { error, errorCode } = await shopNoticeRepository.update(shopId, noticeId, formData);
  if (error !== undefined) return toFailure(errorCode, SHOP_NOTICE_COPY.UPDATE_FAILED);

  revalidatePath(SHOP_PATH);
  return { success: true };
}

export async function deleteNoticeAction(shopId: number, noticeId: number): Promise<ActionResult<never>> {
  const { error, errorCode } = await shopNoticeRepository.remove(shopId, noticeId);
  if (error !== undefined) return toFailure(errorCode, SHOP_NOTICE_COPY.DELETE_FAILED);

  revalidatePath(SHOP_PATH);
  return { success: true };
}

/**
 * 앱 노출 토글.
 *
 * "이 공지를 켜면 기존 노출 공지가 내려간다"는 집합 불변식은 서버 도메인 서비스가 소유하므로
 * 여기서 다른 행을 선판정해 끄지 않는다 — 호출부가 응답 후 목록을 전량 재조회한다.
 */
export async function updateNoticeExposureAction(
  shopId: number,
  noticeId: number,
  exposed: boolean,
): Promise<ActionResult<never>> {
  const { error, errorCode } = await shopNoticeRepository.updateExposure(shopId, noticeId, { exposed });
  if (error !== undefined) return toFailure(errorCode, SHOP_NOTICE_COPY.EXPOSURE_FAILED);

  revalidatePath(SHOP_PATH);
  return { success: true };
}

/**
 * 저장 전 금칙어 사전검사.
 *
 * 실패해도 등록을 막지 않는 보조 기능이라 `success: true` 에 빈 배열을 실어 돌려준다 —
 * 사전검사가 죽었다는 이유로 점주가 공지를 못 올리는 상황을 만들지 않는다.
 */
export async function validateNoticeContentAction(shopId: number, content: string): Promise<ActionResult<string[]>> {
  const parsed = noticeSchema.safeParse({ content });
  if (!parsed.success) return invalidInput(parsed.error.issues[0]?.message);

  const { data, error } = await shopNoticeRepository.validateContent(shopId, { content: parsed.data.content });
  if (error !== undefined) return { success: true, data: [] };

  return { success: true, data: data ?? [] };
}
