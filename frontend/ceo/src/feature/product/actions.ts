"use server";

import { revalidatePath } from "next/cache";

import type { ProductImageListResponse, ProductVegetarianResponse } from "@/api/product/product.dto";
import { productRepository } from "@/api/product/product.repository";

import type {
  AvailabilityChangeOutcome,
  LinkedProductSummary,
  MenuExposure,
  MenuExposureHour,
  MenuImageList,
  MenuVegetarian,
  OptionSelection,
  ProductReleaseTarget,
} from "./domain";
import { PRODUCT_MENU_MESSAGE, PRODUCT_MENU_VALIDATION_MESSAGE, PRODUCT_MESSAGE } from "./message";
import {
  availabilityTargetSchema,
  exposureSaveSchema,
  menuCategoryFormSchema,
  optionAvailabilityTargetSchema,
  orderedIdsSchema,
  releaseTargetSchema,
  shopIdSchema,
  soldOutUntilStringSchema,
  vegetarianFormSchema,
  vegetarianTypeSchema,
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

// =====================================================================================
// 점주 메뉴·옵션 관리 Server Action (`docs/tasks/frontend.md`)
//
// 위 품절·숨김 액션과 같은 규칙이다 — throw 하지 않고 `{ success, message?, data? }` 를 돌려주며,
// 400 은 서버가 내려준 한국어 문구를 그대로 노출한다(프론트에서 errorCode → 문구 맵을 만들지 않는다).
//
// `revalidatePath` 대상이 화면마다 다르다:
// - 메뉴판·상세는 목록과 상세가 서로를 바꾸므로 **메뉴판 세그먼트 전체**를 무효화한다.
// - 옵션그룹 변경은 메뉴 상세의 연결 목록에도 나타나므로 마찬가지다.
// =====================================================================================

const PRODUCT_MENU_PATH = "/dashboard/shop/menus";
const PRODUCT_OPTION_GROUP_PATH = "/dashboard/shop/menus/option-groups";

/**
 * 메뉴판 세그먼트 전체 무효화.
 *
 * `layout` 스코프를 쓰는 이유는 상세(`menus/[productId]`)가 동적 세그먼트라
 * 경로 하나만 무효화하면 방금 바꾼 메뉴의 상세만 갱신되고 목록은 옛 순서를 그대로 보여주기 때문이다.
 */
function revalidateMenuBoard(): void {
  revalidatePath(PRODUCT_MENU_PATH, "layout");
}

/** `{ success: true }` 만 필요한 액션의 결과 */
type SimpleResult = DataResult<never>;

/** 변경 액션의 공통 후처리 — 실패 문구 선택과 revalidate 를 한곳에 모은다 */
function toSimpleResult(error: string | undefined, fallback: string): SimpleResult {
  if (error !== undefined) return toFailure(error, fallback);
  revalidateMenuBoard();
  return { success: true };
}

// ===== 메뉴그룹 (§3) =====

export async function createMenuCategoryAction(
  shopId: number,
  name: string,
  description: string,
): Promise<DataResult<number>> {
  const parsed = shopIdSchema.safeParse(shopId);
  if (!parsed.success) return invalidInput(parsed.error.issues[0]?.message);

  const parsedForm = menuCategoryFormSchema.safeParse({ name, description });
  if (!parsedForm.success) return invalidInput(parsedForm.error.issues[0]?.message);

  const { data, error } = await productRepository.createCategory({
    shopId: parsed.data,
    name: parsedForm.data.name,
    // 빈 문자열을 그대로 보내면 서버가 "설명 있음"으로 저장한다 — 미입력은 생략한다.
    description: parsedForm.data.description.trim() || undefined,
  });
  if (error !== undefined) return toFailure(error, PRODUCT_MENU_MESSAGE.CATEGORY_CREATE_FAILED);

  revalidateMenuBoard();
  return { success: true, data };
}

export async function updateMenuCategoryAction(
  categoryId: number,
  shopId: number,
  name: string,
  description: string,
): Promise<SimpleResult> {
  const parsed = shopIdSchema.safeParse(shopId);
  if (!parsed.success) return invalidInput(parsed.error.issues[0]?.message);

  const parsedForm = menuCategoryFormSchema.safeParse({ name, description });
  if (!parsedForm.success) return invalidInput(parsedForm.error.issues[0]?.message);

  const { error } = await productRepository.updateCategory(categoryId, {
    shopId: parsed.data,
    name: parsedForm.data.name,
    description: parsedForm.data.description.trim() || undefined,
  });
  return toSimpleResult(error, PRODUCT_MENU_MESSAGE.CATEGORY_UPDATE_FAILED);
}

export async function deleteMenuCategoryAction(categoryId: number, shopId: number): Promise<SimpleResult> {
  const parsed = shopIdSchema.safeParse(shopId);
  if (!parsed.success) return invalidInput(parsed.error.issues[0]?.message);

  const { error } = await productRepository.deleteCategory(categoryId, parsed.data);
  return toSimpleResult(error, PRODUCT_MENU_MESSAGE.CATEGORY_DELETE_FAILED);
}

// ===== 메뉴 CRUD (§2) =====

/**
 * 메뉴 등록·수정 공통 본문 조립.
 *
 * 미분류는 `productCategoryId: null` 로 **명시해 보낸다** — 생략하면 수정 시 서버가
 * "변경 없음"으로 볼지 "미분류로 이동"으로 볼지 알 수 없다.
 */
interface MenuSaveInput {
  shopId: number;
  productCategoryId: number | null;
  name: string;
  composition: string;
  description: string;
  originalPrice: number;
  discountPrice: number | null;
  singleServing: boolean;
  spiciness: number | null;
  representative: boolean;
  ratingExcluded: boolean;
}

function toMenuSaveBody(input: MenuSaveInput) {
  return {
    shopId: input.shopId,
    productCategoryId: input.productCategoryId,
    name: input.name.trim(),
    composition: input.composition.trim() || undefined,
    description: input.description.trim() || undefined,
    originalPrice: input.originalPrice,
    discountPrice: input.discountPrice,
    singleServing: input.singleServing,
    spiciness: input.spiciness,
    representative: input.representative,
    ratingExcluded: input.ratingExcluded,
  };
}

export async function createMenuAction(input: MenuSaveInput): Promise<DataResult<number>> {
  const parsed = shopIdSchema.safeParse(input.shopId);
  if (!parsed.success) return invalidInput(parsed.error.issues[0]?.message);

  const { data, error } = await productRepository.createProduct(toMenuSaveBody(input));
  if (error !== undefined) return toFailure(error, PRODUCT_MENU_MESSAGE.MENU_CREATE_FAILED);

  revalidateMenuBoard();
  return { success: true, data };
}

export async function updateMenuAction(productId: number, input: MenuSaveInput): Promise<SimpleResult> {
  const parsed = shopIdSchema.safeParse(input.shopId);
  if (!parsed.success) return invalidInput(parsed.error.issues[0]?.message);

  const { error } = await productRepository.updateProduct(productId, toMenuSaveBody(input));
  return toSimpleResult(error, PRODUCT_MENU_MESSAGE.MENU_UPDATE_FAILED);
}

/**
 * 메뉴 일괄 삭제.
 *
 * 품절·숨김 일괄 처리와 같이 **HTTP 200 + 부분실패**로 돌아온다 — 노출 메뉴 최소 1개,
 * 추천 메뉴 최소 1개 제약에 걸린 항목이 `failed` 에 담긴다(`backend.md` §2-3).
 */
export async function deleteMenusAction(
  shopId: number,
  productIds: number[],
): Promise<DataResult<AvailabilityChangeOutcome>> {
  const parsed = availabilityTargetSchema.safeParse({ shopId, productIds });
  if (!parsed.success) return invalidInput(parsed.error.issues[0]?.message);

  const { data, error } = await productRepository.deleteProducts({
    shopId: parsed.data.shopId,
    productIds: parsed.data.productIds,
  });
  if (error !== undefined) return toFailure(error, PRODUCT_MENU_MESSAGE.MENU_DELETE_FAILED);

  revalidateMenuBoard();
  return { success: true, data: toOutcome(data) };
}

// ===== 순서 변경 (§4) =====

export async function changeMenuCategoryOrderAction(
  shopId: number,
  productCategoryIds: number[],
): Promise<SimpleResult> {
  const parsed = shopIdSchema.safeParse(shopId);
  if (!parsed.success) return invalidInput(parsed.error.issues[0]?.message);

  const parsedIds = orderedIdsSchema.safeParse(productCategoryIds);
  if (!parsedIds.success) return invalidInput();

  const { error } = await productRepository.changeCategoryOrder({
    shopId: parsed.data,
    productCategoryIds: parsedIds.data,
  });
  return toSimpleResult(error, PRODUCT_MENU_MESSAGE.ORDER_CHANGE_FAILED);
}

export async function changeMenuOrderAction(
  shopId: number,
  productCategoryId: number | null,
  productIds: number[],
): Promise<SimpleResult> {
  const parsed = shopIdSchema.safeParse(shopId);
  if (!parsed.success) return invalidInput(parsed.error.issues[0]?.message);

  const parsedIds = orderedIdsSchema.safeParse(productIds);
  if (!parsedIds.success) return invalidInput();

  const { error } = await productRepository.changeProductOrder({
    shopId: parsed.data,
    productCategoryId,
    productIds: parsedIds.data,
  });
  return toSimpleResult(error, PRODUCT_MENU_MESSAGE.ORDER_CHANGE_FAILED);
}

/**
 * 메뉴를 다른 그룹으로 이동.
 *
 * `targetOrderedProductIds` 를 반드시 함께 보낸다 — 빠뜨리면 서버가 도착 그룹 맨 끝에 append 해
 * 사용자가 드롭한 위치가 무시된다(`backend.md` §4-3).
 */
export async function moveMenuCategoryAction(
  shopId: number,
  targetProductCategoryId: number | null,
  productIds: number[],
  targetOrderedProductIds: number[],
): Promise<SimpleResult> {
  const parsed = shopIdSchema.safeParse(shopId);
  if (!parsed.success) return invalidInput(parsed.error.issues[0]?.message);

  const parsedIds = orderedIdsSchema.safeParse(productIds);
  const parsedTargetIds = orderedIdsSchema.safeParse(targetOrderedProductIds);
  if (!parsedIds.success || !parsedTargetIds.success) return invalidInput();

  const { error } = await productRepository.moveProductCategory({
    shopId: parsed.data,
    targetProductCategoryId,
    productIds: parsedIds.data,
    targetOrderedProductIds: parsedTargetIds.data,
  });
  return toSimpleResult(error, PRODUCT_MENU_MESSAGE.CATEGORY_MOVE_FAILED);
}

// ===== 옵션그룹 · 옵션 (§5) =====

interface OptionGroupSaveInput {
  shopId: number;
  name: string;
  description: string;
  required: boolean;
  multipleSelect: boolean;
  minSelect: number | null;
  maxSelect: number | null;
}

function toOptionGroupBody(input: OptionGroupSaveInput) {
  return {
    shopId: input.shopId,
    name: input.name.trim(),
    description: input.description.trim() || undefined,
    required: input.required,
    multipleSelect: input.multipleSelect,
    minSelect: input.minSelect,
    maxSelect: input.maxSelect,
  };
}

export async function createOptionGroupAction(input: OptionGroupSaveInput): Promise<DataResult<number>> {
  const parsed = shopIdSchema.safeParse(input.shopId);
  if (!parsed.success) return invalidInput(parsed.error.issues[0]?.message);

  const { data, error } = await productRepository.createOptionGroup(toOptionGroupBody(input));
  if (error !== undefined) return toFailure(error, PRODUCT_MENU_MESSAGE.OPTION_GROUP_CREATE_FAILED);

  revalidatePath(PRODUCT_OPTION_GROUP_PATH);
  revalidateMenuBoard();
  return { success: true, data };
}

export async function updateOptionGroupAction(
  optionGroupId: number,
  input: OptionGroupSaveInput,
): Promise<SimpleResult> {
  const parsed = shopIdSchema.safeParse(input.shopId);
  if (!parsed.success) return invalidInput(parsed.error.issues[0]?.message);

  const { error } = await productRepository.updateOptionGroup(optionGroupId, toOptionGroupBody(input));
  if (error !== undefined) return toFailure(error, PRODUCT_MENU_MESSAGE.OPTION_GROUP_UPDATE_FAILED);

  revalidatePath(PRODUCT_OPTION_GROUP_PATH);
  revalidateMenuBoard();
  return { success: true };
}

export async function deleteOptionGroupAction(optionGroupId: number, shopId: number): Promise<SimpleResult> {
  const parsed = shopIdSchema.safeParse(shopId);
  if (!parsed.success) return invalidInput(parsed.error.issues[0]?.message);

  const { error } = await productRepository.deleteOptionGroup(optionGroupId, parsed.data);
  if (error !== undefined) return toFailure(error, PRODUCT_MENU_MESSAGE.OPTION_GROUP_DELETE_FAILED);

  revalidatePath(PRODUCT_OPTION_GROUP_PATH);
  revalidateMenuBoard();
  return { success: true };
}

export async function createOptionAction(
  optionGroupId: number,
  shopId: number,
  name: string,
  additionalPrice: number,
): Promise<DataResult<number>> {
  const parsed = shopIdSchema.safeParse(shopId);
  if (!parsed.success) return invalidInput(parsed.error.issues[0]?.message);

  const { data, error } = await productRepository.createOption(optionGroupId, {
    shopId: parsed.data,
    name: name.trim(),
    additionalPrice,
  });
  if (error !== undefined) return toFailure(error, PRODUCT_MENU_MESSAGE.OPTION_CREATE_FAILED);

  revalidatePath(PRODUCT_OPTION_GROUP_PATH);
  return { success: true, data };
}

export async function updateOptionAction(
  optionId: number,
  shopId: number,
  name: string,
  additionalPrice: number,
): Promise<SimpleResult> {
  const parsed = shopIdSchema.safeParse(shopId);
  if (!parsed.success) return invalidInput(parsed.error.issues[0]?.message);

  const { error } = await productRepository.updateOption(optionId, {
    shopId: parsed.data,
    name: name.trim(),
    additionalPrice,
  });
  if (error !== undefined) return toFailure(error, PRODUCT_MENU_MESSAGE.OPTION_UPDATE_FAILED);

  revalidatePath(PRODUCT_OPTION_GROUP_PATH);
  return { success: true };
}

export async function deleteOptionAction(optionId: number, shopId: number): Promise<SimpleResult> {
  const parsed = shopIdSchema.safeParse(shopId);
  if (!parsed.success) return invalidInput(parsed.error.issues[0]?.message);

  const { error } = await productRepository.deleteOption(optionId, parsed.data);
  if (error !== undefined) return toFailure(error, PRODUCT_MENU_MESSAGE.OPTION_DELETE_FAILED);

  revalidatePath(PRODUCT_OPTION_GROUP_PATH);
  return { success: true };
}

export async function changeOptionOrderAction(
  optionGroupId: number,
  shopId: number,
  optionIds: number[],
): Promise<SimpleResult> {
  const parsed = shopIdSchema.safeParse(shopId);
  if (!parsed.success) return invalidInput(parsed.error.issues[0]?.message);

  const parsedIds = orderedIdsSchema.safeParse(optionIds);
  if (!parsedIds.success) return invalidInput();

  const { error } = await productRepository.changeOptionOrder(optionGroupId, {
    shopId: parsed.data,
    optionIds: parsedIds.data,
  });
  if (error !== undefined) return toFailure(error, PRODUCT_MENU_MESSAGE.ORDER_CHANGE_FAILED);

  revalidatePath(PRODUCT_OPTION_GROUP_PATH);
  return { success: true };
}

// ===== 메뉴-옵션그룹 연결 (§5-2) =====

export async function linkOptionGroupAction(
  productId: number,
  optionGroupId: number,
  shopId: number,
): Promise<SimpleResult> {
  const parsed = shopIdSchema.safeParse(shopId);
  if (!parsed.success) return invalidInput(parsed.error.issues[0]?.message);

  const { error } = await productRepository.linkOptionGroup(productId, optionGroupId, parsed.data);
  return toSimpleResult(error, PRODUCT_MENU_MESSAGE.OPTION_GROUP_LINK_FAILED);
}

/**
 * 연결 해제.
 *
 * **마지막 연결은 서버가 막는다**(`PRODUCT_OPTION_GROUP_LAST_LINK_CANNOT_UNLINK`) — 연결이 0건이면
 * 어디서도 보이지 않는 고아 그룹이 되기 때문이다. UI 도 버튼을 비활성화하지만 최종 판정은 서버다.
 */
export async function unlinkOptionGroupAction(
  productId: number,
  optionGroupId: number,
  shopId: number,
): Promise<SimpleResult> {
  const parsed = shopIdSchema.safeParse(shopId);
  if (!parsed.success) return invalidInput(parsed.error.issues[0]?.message);

  const { error } = await productRepository.unlinkOptionGroup(productId, optionGroupId, parsed.data);
  return toSimpleResult(error, PRODUCT_MENU_MESSAGE.OPTION_GROUP_UNLINK_FAILED);
}

export async function changeLinkedOptionGroupOrderAction(
  productId: number,
  shopId: number,
  optionGroupIds: number[],
): Promise<SimpleResult> {
  const parsed = shopIdSchema.safeParse(shopId);
  if (!parsed.success) return invalidInput(parsed.error.issues[0]?.message);

  const parsedIds = orderedIdsSchema.safeParse(optionGroupIds);
  if (!parsedIds.success) return invalidInput();

  const { error } = await productRepository.changeLinkedOptionGroupOrder(productId, {
    shopId: parsed.data,
    optionGroupIds: parsedIds.data,
  });
  return toSimpleResult(error, PRODUCT_MENU_MESSAGE.ORDER_CHANGE_FAILED);
}

/** 해제 전 영향 확인. 조회라 revalidate 하지 않는다 */
export async function loadOptionGroupLinkedProductsAction(
  optionGroupId: number,
  shopId: number,
): Promise<DataResult<LinkedProductSummary[]>> {
  const parsed = shopIdSchema.safeParse(shopId);
  if (!parsed.success) return invalidInput(parsed.error.issues[0]?.message);

  const { data, error } = await productRepository.getOptionGroupLinkedProducts(optionGroupId, parsed.data);
  if (error !== undefined) return toFailure(error, PRODUCT_MENU_MESSAGE.LINKED_PRODUCTS_LOAD_FAILED);

  return { success: true, data: data ?? [] };
}

// ===== 노출기간 (§6) =====

/** Sheet 를 열 때 현재 설정을 읽는다 — 상세 페이지에서 미리 받아오면 Sheet 마다 페이로드가 커진다 */
export async function loadMenuExposureAction(productId: number, shopId: number): Promise<DataResult<MenuExposure>> {
  const parsed = shopIdSchema.safeParse(shopId);
  if (!parsed.success) return invalidInput(parsed.error.issues[0]?.message);

  const { data, error } = await productRepository.getExposure(productId, parsed.data);
  if (error !== undefined || !data) return toFailure(error, PRODUCT_MENU_MESSAGE.EXPOSURE_LOAD_FAILED);

  return { success: true, data };
}

export async function saveMenuExposureAction(
  productId: number,
  shopId: number,
  input: { startDate: string | null; endDate: string | null; hours: MenuExposureHour[] },
): Promise<SimpleResult> {
  const parsed = shopIdSchema.safeParse(shopId);
  if (!parsed.success) return invalidInput(parsed.error.issues[0]?.message);

  const parsedInput = exposureSaveSchema.safeParse(input);
  if (!parsedInput.success) return invalidInput(parsedInput.error.issues[0]?.message);

  const { error } = await productRepository.changeExposure(productId, {
    shopId: parsed.data,
    startDate: parsedInput.data.startDate,
    endDate: parsedInput.data.endDate,
    hours: parsedInput.data.hours,
  });
  return toSimpleResult(error, PRODUCT_MENU_MESSAGE.EXPOSURE_SAVE_FAILED);
}

/** 상시 노출로 복귀 — 스케줄 행을 전부 지운다 */
export async function clearMenuExposureAction(productId: number, shopId: number): Promise<SimpleResult> {
  const parsed = shopIdSchema.safeParse(shopId);
  if (!parsed.success) return invalidInput(parsed.error.issues[0]?.message);

  const { error } = await productRepository.clearExposure(productId, parsed.data);
  return toSimpleResult(error, PRODUCT_MENU_MESSAGE.EXPOSURE_CLEAR_FAILED);
}

// ===== 이미지 (§7-1) =====

/**
 * 서버는 요청 이력(`requests`)만 내려주므로, UI 가 단일 값으로 읽을 수 있도록
 * 검수 대기(우선)·반려 건을 `pendingRequest` 로 뽑아 준다 — 채식(`MenuVegetarian.pendingRequest`)과 같은 규약.
 */
function toMenuImageList(response: ProductImageListResponse): MenuImageList {
  const requests = response.requests ?? [];
  const pendingRequest =
    requests.find((request) => request.status === "PENDING") ??
    requests.find((request) => request.status === "REJECTED") ??
    null;

  return {
    images: response.images,
    requests,
    pendingRequest,
  };
}

export async function loadMenuImagesAction(productId: number, shopId: number): Promise<DataResult<MenuImageList>> {
  const parsed = shopIdSchema.safeParse(shopId);
  if (!parsed.success) return invalidInput(parsed.error.issues[0]?.message);

  const { data, error } = await productRepository.getProductImages(productId, parsed.data);
  if (error !== undefined || !data) return toFailure(error, PRODUCT_MENU_MESSAGE.IMAGE_LOAD_FAILED);

  return { success: true, data: toMenuImageList(data) };
}

/**
 * 이미지 등록 요청.
 *
 * `FormData` 를 그대로 받는다 — Server Action 경계를 넘는 `File` 은 직렬화되지만, 클라이언트가
 * 이미 `FormData` 를 들고 있으므로 다시 풀어 담을 이유가 없다.
 * **규격 판정은 서버**다(`PRODUCT_IMAGE_SPEC_INVALID`) — 브라우저에서 해상도를 재도 서버가 다시 본다.
 */
export async function requestMenuImageAction(
  productId: number,
  shopId: number,
  formData: FormData,
): Promise<SimpleResult> {
  const parsed = shopIdSchema.safeParse(shopId);
  if (!parsed.success) return invalidInput(parsed.error.issues[0]?.message);

  const file = formData.get("file");
  if (!(file instanceof File) || file.size === 0) return invalidInput(PRODUCT_MENU_MESSAGE.IMAGE_FILE_REQUIRED);

  const { error } = await productRepository.requestProductImage(productId, parsed.data, file);
  return toSimpleResult(error, PRODUCT_MENU_MESSAGE.IMAGE_REQUEST_FAILED);
}

export async function changeMenuImageOrderAction(
  productId: number,
  shopId: number,
  imageIds: number[],
): Promise<SimpleResult> {
  const parsed = shopIdSchema.safeParse(shopId);
  if (!parsed.success) return invalidInput(parsed.error.issues[0]?.message);

  const parsedIds = orderedIdsSchema.safeParse(imageIds);
  if (!parsedIds.success) return invalidInput();

  const { error } = await productRepository.changeProductImageOrder(productId, {
    shopId: parsed.data,
    imageIds: parsedIds.data,
  });
  return toSimpleResult(error, PRODUCT_MENU_MESSAGE.IMAGE_ORDER_FAILED);
}

export async function deleteMenuImageAction(imageId: number, shopId: number): Promise<SimpleResult> {
  const parsed = shopIdSchema.safeParse(shopId);
  if (!parsed.success) return invalidInput(parsed.error.issues[0]?.message);

  const { error } = await productRepository.deleteProductImage(imageId, parsed.data);
  return toSimpleResult(error, PRODUCT_MENU_MESSAGE.IMAGE_DELETE_FAILED);
}

// ===== 채식 (§7-1) =====

/**
 * 서버는 요청 이력(`requests`)만 내려주므로, UI 가 단일 값으로 읽을 수 있도록
 * 검수 대기(우선)·반려 건을 `pendingRequest` 로 뽑아 준다 — 이미지(`MenuImageList.pendingRequest`)와 같은 규약.
 */
function toMenuVegetarian(response: ProductVegetarianResponse): MenuVegetarian {
  const requests = response.requests ?? [];
  const pendingRequest =
    requests.find((request) => request.status === "PENDING") ??
    requests.find((request) => request.status === "REJECTED") ??
    null;

  return {
    vegetarianType: response.vegetarianType,
    requests,
    pendingRequest,
    changeable: response.changeable,
  };
}

export async function loadMenuVegetarianAction(productId: number, shopId: number): Promise<DataResult<MenuVegetarian>> {
  const parsed = shopIdSchema.safeParse(shopId);
  if (!parsed.success) return invalidInput(parsed.error.issues[0]?.message);

  const { data, error } = await productRepository.getVegetarian(productId, parsed.data);
  if (error !== undefined || !data) return toFailure(error, PRODUCT_MENU_MESSAGE.VEGETARIAN_LOAD_FAILED);

  return { success: true, data: toMenuVegetarian(data) };
}

export async function requestMenuVegetarianAction(
  productId: number,
  shopId: number,
  input: { vegetarianType: string; ingredients: string; description: string },
): Promise<SimpleResult> {
  const parsed = shopIdSchema.safeParse(shopId);
  if (!parsed.success) return invalidInput(parsed.error.issues[0]?.message);

  const parsedType = vegetarianTypeSchema.safeParse(input.vegetarianType);
  if (!parsedType.success) return invalidInput(PRODUCT_MENU_VALIDATION_MESSAGE.VEGETARIAN_TYPE_REQUIRED);

  const parsedForm = vegetarianFormSchema.safeParse(input);
  if (!parsedForm.success) return invalidInput(parsedForm.error.issues[0]?.message);

  const { error } = await productRepository.requestVegetarian(productId, {
    shopId: parsed.data,
    vegetarianType: parsedType.data,
    ingredients: parsedForm.data.ingredients,
    description: parsedForm.data.description.trim() || undefined,
  });
  return toSimpleResult(error, PRODUCT_MENU_MESSAGE.VEGETARIAN_REQUEST_FAILED);
}

export async function clearMenuVegetarianAction(productId: number, shopId: number): Promise<SimpleResult> {
  const parsed = shopIdSchema.safeParse(shopId);
  if (!parsed.success) return invalidInput(parsed.error.issues[0]?.message);

  const { error } = await productRepository.clearVegetarian(productId, parsed.data);
  return toSimpleResult(error, PRODUCT_MENU_MESSAGE.VEGETARIAN_CLEAR_FAILED);
}
