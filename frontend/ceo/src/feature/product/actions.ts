"use server";

import { revalidatePath } from "next/cache";

import { productRepository } from "@/api/product/product.repository";

import type { AvailabilityChangeOutcome, OptionSelection, ProductReleaseTarget } from "./domain";
import { PRODUCT_MESSAGE } from "./message";
import {
  availabilityTargetSchema,
  optionAvailabilityTargetSchema,
  releaseTargetSchema,
  soldOutUntilStringSchema,
} from "./schema";

const PRODUCT_AVAILABILITY_PATH = "/dashboard/shop/menus/availability";

/**
 * 액션 결과.
 *
 * **부분실패 정보가 `data.failed` 에 담겨 오므로 `success` 만 담는 `ActionResult` 로는 부족하다.**
 * 부분실패는 HTTP 200 이라 `success: true` 로 돌아오고, UI 가 `data.failed.length` 로 안내를 가른다.
 */
type DataResult<T> = {
  success: boolean;
  message?: string;
  data?: T;
};

function invalidInput(message?: string): DataResult<never> {
  return { success: false, message: message ?? PRODUCT_MESSAGE.INVALID_INPUT };
}

/**
 * 서버 실패를 문구로 바꾼다.
 *
 * 이 화면의 400 은 `PRODUCT_SOLD_OUT_UNTIL_TOO_SOON` 처럼 **사용자가 고칠 수 있는 상황**이고
 * 백엔드가 이미 한국어 문구를 내려주므로(`docs/tasks/backend.md` §2-5 에러 표) 그대로 노출한다.
 * 프론트에서 errorCode → 문구 표를 다시 만들면 서버가 문구를 다듬을 때마다 어긋난다.
 */
function toFailure(error: string | undefined, fallback: string): DataResult<never> {
  return { success: false, message: error ?? fallback };
}

/** 일괄 처리 응답은 전부 같은 모양이라 후처리를 한곳에 모은다 */
function toOutcome(data: AvailabilityChangeOutcome | undefined): AvailabilityChangeOutcome {
  return { succeededIds: data?.succeededIds ?? [], failed: data?.failed ?? [] };
}

// ===== 메뉴 =====

/**
 * 메뉴 일괄 품절.
 *
 * `soldOutUntil` 을 보내지 않으면 **서버가 영업시간 기반 다음 오픈 시각으로 채운다.**
 * 클라이언트가 계산하려면 영업시간·휴무일·공휴일을 모두 알아야 하므로 기본값을 만들지 않는다.
 */
export async function markProductsSoldOutAction(
  shopId: number,
  productIds: number[],
  soldOutUntil?: string,
): Promise<DataResult<AvailabilityChangeOutcome>> {
  const parsed = availabilityTargetSchema.safeParse({ shopId, productIds });
  if (!parsed.success) return invalidInput(parsed.error.issues[0]?.message);

  if (soldOutUntil !== undefined) {
    const parsedUntil = soldOutUntilStringSchema.safeParse(soldOutUntil);
    if (!parsedUntil.success) return invalidInput(parsedUntil.error.issues[0]?.message);
  }

  const { data, error } = await productRepository.markSoldOut({
    shopId: parsed.data.shopId,
    productIds: parsed.data.productIds,
    soldOutUntil,
  });
  if (error !== undefined) return toFailure(error, PRODUCT_MESSAGE.SOLD_OUT_FAILED);

  revalidatePath(PRODUCT_AVAILABILITY_PATH);
  return { success: true, data: toOutcome(data) };
}

export async function hideProductsAction(
  shopId: number,
  productIds: number[],
): Promise<DataResult<AvailabilityChangeOutcome>> {
  const parsed = availabilityTargetSchema.safeParse({ shopId, productIds });
  if (!parsed.success) return invalidInput(parsed.error.issues[0]?.message);

  const { data, error } = await productRepository.hide({
    shopId: parsed.data.shopId,
    productIds: parsed.data.productIds,
  });
  if (error !== undefined) return toFailure(error, PRODUCT_MESSAGE.HIDE_FAILED);

  revalidatePath(PRODUCT_AVAILABILITY_PATH);
  return { success: true, data: toOutcome(data) };
}

export async function releaseProductsAction(
  shopId: number,
  productIds: number[],
  target: ProductReleaseTarget,
): Promise<DataResult<AvailabilityChangeOutcome>> {
  const parsed = availabilityTargetSchema.safeParse({ shopId, productIds });
  if (!parsed.success) return invalidInput(parsed.error.issues[0]?.message);

  const parsedTarget = releaseTargetSchema.safeParse(target);
  if (!parsedTarget.success) return invalidInput();

  const { data, error } = await productRepository.release({
    shopId: parsed.data.shopId,
    productIds: parsed.data.productIds,
    target: parsedTarget.data,
  });
  if (error !== undefined) return toFailure(error, PRODUCT_MESSAGE.RELEASE_FAILED);

  revalidatePath(PRODUCT_AVAILABILITY_PATH);
  return { success: true, data: toOutcome(data) };
}

export async function changeProductsSoldOutUntilAction(
  shopId: number,
  productIds: number[],
  soldOutUntil: string,
): Promise<DataResult<AvailabilityChangeOutcome>> {
  const parsed = availabilityTargetSchema.safeParse({ shopId, productIds });
  if (!parsed.success) return invalidInput(parsed.error.issues[0]?.message);

  const parsedUntil = soldOutUntilStringSchema.safeParse(soldOutUntil);
  if (!parsedUntil.success) return invalidInput(parsedUntil.error.issues[0]?.message);

  const { data, error } = await productRepository.changeSoldOutUntil({
    shopId: parsed.data.shopId,
    productIds: parsed.data.productIds,
    soldOutUntil: parsedUntil.data,
  });
  if (error !== undefined) return toFailure(error, PRODUCT_MESSAGE.PERIOD_CHANGE_FAILED);

  revalidatePath(PRODUCT_AVAILABILITY_PATH);
  return { success: true, data: toOutcome(data) };
}

// ===== 옵션 =====

export async function markOptionsSoldOutAction(
  shopId: number,
  options: OptionSelection[],
  soldOutUntil?: string,
): Promise<DataResult<AvailabilityChangeOutcome>> {
  const parsed = optionAvailabilityTargetSchema.safeParse({ shopId, options });
  if (!parsed.success) return invalidInput(parsed.error.issues[0]?.message);

  if (soldOutUntil !== undefined) {
    const parsedUntil = soldOutUntilStringSchema.safeParse(soldOutUntil);
    if (!parsedUntil.success) return invalidInput(parsedUntil.error.issues[0]?.message);
  }

  const { data, error } = await productRepository.markOptionsSoldOut({
    shopId: parsed.data.shopId,
    options: parsed.data.options,
    soldOutUntil,
  });
  if (error !== undefined) return toFailure(error, PRODUCT_MESSAGE.SOLD_OUT_FAILED);

  revalidatePath(PRODUCT_AVAILABILITY_PATH);
  return { success: true, data: toOutcome(data) };
}

export async function hideOptionsAction(
  shopId: number,
  options: OptionSelection[],
): Promise<DataResult<AvailabilityChangeOutcome>> {
  const parsed = optionAvailabilityTargetSchema.safeParse({ shopId, options });
  if (!parsed.success) return invalidInput(parsed.error.issues[0]?.message);

  const { data, error } = await productRepository.hideOptions({
    shopId: parsed.data.shopId,
    options: parsed.data.options,
  });
  if (error !== undefined) return toFailure(error, PRODUCT_MESSAGE.HIDE_FAILED);

  revalidatePath(PRODUCT_AVAILABILITY_PATH);
  return { success: true, data: toOutcome(data) };
}

export async function releaseOptionsAction(
  shopId: number,
  options: OptionSelection[],
  target: ProductReleaseTarget,
): Promise<DataResult<AvailabilityChangeOutcome>> {
  const parsed = optionAvailabilityTargetSchema.safeParse({ shopId, options });
  if (!parsed.success) return invalidInput(parsed.error.issues[0]?.message);

  const parsedTarget = releaseTargetSchema.safeParse(target);
  if (!parsedTarget.success) return invalidInput();

  const { data, error } = await productRepository.releaseOptions({
    shopId: parsed.data.shopId,
    options: parsed.data.options,
    target: parsedTarget.data,
  });
  if (error !== undefined) return toFailure(error, PRODUCT_MESSAGE.RELEASE_FAILED);

  revalidatePath(PRODUCT_AVAILABILITY_PATH);
  return { success: true, data: toOutcome(data) };
}

export async function changeOptionsSoldOutUntilAction(
  shopId: number,
  options: OptionSelection[],
  soldOutUntil: string,
): Promise<DataResult<AvailabilityChangeOutcome>> {
  const parsed = optionAvailabilityTargetSchema.safeParse({ shopId, options });
  if (!parsed.success) return invalidInput(parsed.error.issues[0]?.message);

  const parsedUntil = soldOutUntilStringSchema.safeParse(soldOutUntil);
  if (!parsedUntil.success) return invalidInput(parsedUntil.error.issues[0]?.message);

  const { data, error } = await productRepository.changeOptionsSoldOutUntil({
    shopId: parsed.data.shopId,
    options: parsed.data.options,
    soldOutUntil: parsedUntil.data,
  });
  if (error !== undefined) return toFailure(error, PRODUCT_MESSAGE.PERIOD_CHANGE_FAILED);

  revalidatePath(PRODUCT_AVAILABILITY_PATH);
  return { success: true, data: toOutcome(data) };
}
