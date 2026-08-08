"use server";

import { revalidatePath } from "next/cache";

import { ALLOWED_IMAGE_TYPES, MAX_IMAGE_SIZE_BYTES } from "@/api/file/file.dto";
import { shopRepository } from "@/api/shop/shop.repository";
import { shopService } from "@/api/shop/shop.service";
import type {
  AmenityCategory,
  BusinessHour,
  ContentBoardItem,
  PhoneNumber,
  ShopDeliveryArea,
  ShopDeliveryTipSetting,
  ShopOperationInfo,
  ShopSummary,
  Suspension,
} from "@/feature/shop/domain";

import { CONTENT_BOARD_MAX_COUNT, PHONE_NUMBER_MAX_COUNT, REGULAR_CLOSED_DAY_MAX_COUNT } from "./constants";
import { SHOP_MESSAGE } from "./message";
import {
  type BusinessHourValues,
  businessHourSchema,
  type ClosedDayFormValues,
  CONTENT_BOARD_LIMIT_MESSAGE,
  type ConvenienceInfoFormValues,
  closedDaySchema,
  contentBoardSchema,
  convenienceInfoSchema,
  type DayTimeRangeValues,
  type DeliveryTipDistanceFormValues,
  type DeliveryTipHolidayFormValues,
  type DeliveryTipRegionsFormValues,
  type DeliveryTipSchedulesFormValues,
  type DeliveryTipTiersFormValues,
  dayTimeRangeSchema,
  deliveryTipDistanceSchema,
  deliveryTipHolidaySchema,
  deliveryTipRegionsSchema,
  deliveryTipSchedulesSchema,
  deliveryTipTiersSchema,
  type HolidayClosedFormValues,
  holidayClosedSchema,
  type PhoneNumberFormValues,
  phoneNumberSchema,
  type ShopIntroductionFormValues,
  type ShopMinOrderAmountFormValues,
  type ShopRiderPickupLocationFormValues,
  type ShopRiderVisitGuideFormValues,
  type ShopScheduledOrderFormValues,
  type ShopStatusFormValues,
  type SuspensionFormValues,
  shopIntroductionSchema,
  shopMinOrderAmountSchema,
  shopRiderPickupLocationSchema,
  shopRiderVisitGuideSchema,
  shopScheduledOrderSchema,
  shopStatusSchema,
  suspensionSchema,
  type TemporaryClosureFormValues,
  temporaryClosureSchema,
} from "./schema";
import { toLocalDateTimeString } from "./time";

const SHOP_PATH = "/dashboard/shop";
const SHOP_STATUS_PATH = "/dashboard/shop-status";

type ActionResult = {
  success: boolean;
  message?: string;
  id?: number;
};

type DataResult<T> = {
  success: boolean;
  message?: string;
  data?: T;
};

const invalidInput = (message?: string): ActionResult => ({
  success: false,
  message: message ?? SHOP_MESSAGE.INVALID_INPUT,
});

// ===== 이미지 검증 헬퍼 =====

// 클라이언트에서 규격을 선검사하더라도 MIME/용량은 서버에서 다시 확인한다.
function assertUploadableImage(file: FormDataEntryValue | null): file is File {
  return file instanceof File && file.size > 0;
}

function extractFile(formData: FormData): { file: File } | { error: string } {
  const file = formData.get("file");
  if (!assertUploadableImage(file)) return { error: SHOP_MESSAGE.IMAGE_REQUIRED };
  if (!ALLOWED_IMAGE_TYPES.includes(file.type as (typeof ALLOWED_IMAGE_TYPES)[number])) {
    return { error: SHOP_MESSAGE.UPLOAD_FAILED };
  }
  if (file.size > MAX_IMAGE_SIZE_BYTES) return { error: SHOP_MESSAGE.UPLOAD_FAILED };
  return { file };
}

// ===== 조회 (시트 내부 재조회용) =====

export async function fetchMyShopsAction(): Promise<DataResult<ShopSummary[]>> {
  const { error, data } = await shopService.getMyShops({}, { page: 0, size: 100 });
  if (error !== undefined) return { success: false, message: error };
  return { success: true, data: data ?? [] };
}

export async function fetchPhoneNumbersAction(shopId: number): Promise<DataResult<PhoneNumber[]>> {
  const { error, data } = await shopRepository.getPhoneNumbers(shopId);
  if (error !== undefined) return { success: false, message: error };
  return {
    success: true,
    data: (data ?? []).map((item) => ({
      id: item.id,
      phoneNumber: item.phoneNumber,
      primary: item.primary,
      virtual: item.virtual,
    })),
  };
}

export async function fetchContentBoardsAction(shopId: number): Promise<DataResult<ContentBoardItem[]>> {
  const { error, data } = await shopRepository.getContentBoards(shopId);
  if (error !== undefined) return { success: false, message: error };
  return {
    success: true,
    data: (data ?? []).map((item) => ({
      id: item.id,
      contentType: item.contentType,
      topic: item.topic,
      imageUrl: item.imageUrl,
      youtubeUrl: item.youtubeUrl,
      description: item.description,
      hidden: item.hidden,
    })),
  };
}

export async function fetchBusinessHoursAction(shopId: number): Promise<DataResult<BusinessHour[]>> {
  const { error, data } = await shopRepository.getBusinessHours(shopId);
  if (error !== undefined) return { success: false, message: error };
  return {
    success: true,
    data: (data ?? []).map((item) => ({
      id: item.id,
      dayType: item.dayType,
      description: item.description,
      openTime: item.openTime,
      closeTime: item.closeTime,
      isClosed: item.isClosed,
      is24Hours: item.is24Hours,
    })),
  };
}

export async function fetchOperationInfoAction(shopId: number): Promise<DataResult<ShopOperationInfo>> {
  const { error, data } = await shopService.getShopOperationInfo(shopId);
  if (error !== undefined) return { success: false, message: error };
  return { success: true, data };
}

export async function fetchAmenityCategoriesAction(): Promise<DataResult<AmenityCategory[]>> {
  const { error, data } = await shopRepository.getAmenityCategories();
  if (error !== undefined) return { success: false, message: error };
  return {
    success: true,
    data: (data ?? []).map((item) => ({
      id: item.id,
      amenity: item.amenity,
      displayName: item.displayName,
      activeFilePath: item.activeFilePath,
    })),
  };
}

export async function fetchShopSuspensionsAction(shopId: number): Promise<DataResult<Suspension[]>> {
  const { error, data } = await shopRepository.getSuspensions(shopId);
  if (error !== undefined) return { success: false, message: error };
  return {
    success: true,
    data: (data ?? []).map((item) => ({
      id: item.id,
      shopId: item.shopId,
      reason: item.reason,
      orderMethod: item.orderMethod,
      startAt: item.startAt,
      endAt: item.endAt,
      releasedAt: item.releasedAt,
    })),
  };
}

// ===== 기본정보 =====

export async function requestThumbnailChangeAction(shopId: number, formData: FormData): Promise<ActionResult> {
  const extracted = extractFile(formData);
  if ("error" in extracted) return { success: false, message: extracted.error };

  const { error } = await shopRepository.createThumbnailRequest(shopId, extracted.file);
  if (error !== undefined) return { success: false, message: error };

  revalidatePath(SHOP_PATH);
  return { success: true };
}

export async function requestTrademarkChangeAction(shopId: number, formData: FormData): Promise<ActionResult> {
  const extracted = extractFile(formData);
  if ("error" in extracted) return { success: false, message: extracted.error };

  const { error } = await shopRepository.createTrademarkRequest(shopId, extracted.file);
  if (error !== undefined) return { success: false, message: error };

  revalidatePath(SHOP_PATH);
  return { success: true };
}

export async function validateShopIntroductionAction(
  shopId: number,
  values: ShopIntroductionFormValues,
): Promise<DataResult<{ valid: boolean; violations: string[] }>> {
  const parsed = shopIntroductionSchema.safeParse(values);
  if (!parsed.success) return { success: false, message: parsed.error.issues[0]?.message };

  const { error, data } = await shopRepository.validateIntroduction(shopId, parsed.data);
  if (error !== undefined || data === undefined) return { success: false, message: error };

  return { success: true, data };
}

export async function updateShopIntroductionAction(
  shopId: number,
  values: ShopIntroductionFormValues,
): Promise<ActionResult> {
  const parsed = shopIntroductionSchema.safeParse(values);
  if (!parsed.success) return invalidInput(parsed.error.issues[0]?.message);

  const { error } = await shopRepository.updateIntroduction(shopId, parsed.data);
  if (error !== undefined) return { success: false, message: error };

  revalidatePath(SHOP_PATH);
  return { success: true };
}

export async function createContentBoardAction(shopId: number, formData: FormData): Promise<ActionResult> {
  const contentType = formData.get("contentType");
  const topic = formData.get("topic");
  const description = formData.get("description");
  const youtubeUrl = formData.get("youtubeUrl");
  const file = formData.get("file");

  const parsed = contentBoardSchema.safeParse({
    contentType,
    topic,
    description,
    youtubeUrl: youtubeUrl ? String(youtubeUrl) : undefined,
    hasExistingFile: file instanceof File && file.size > 0,
    // FormData 값은 신뢰할 수 없는 문자열이므로 타입 단정 없이 스키마 검증에 그대로 넘긴다.
  });
  if (!parsed.success) return invalidInput(parsed.error.issues[0]?.message);

  // 클라이언트의 로컬 목록은 다른 탭/동시 요청과 어긋날 수 있어, 등록 직전 서버에서 최신 건수를 다시 확인한다.
  const existing = await shopRepository.getContentBoards(shopId);
  if (existing.error !== undefined) return { success: false, message: existing.error };
  if ((existing.data?.length ?? 0) >= CONTENT_BOARD_MAX_COUNT) {
    return { success: false, message: CONTENT_BOARD_LIMIT_MESSAGE };
  }

  const { error, data } = await shopRepository.createContentBoard(
    shopId,
    {
      contentType: parsed.data.contentType,
      topic: parsed.data.topic,
      description: parsed.data.description,
      youtubeUrl: parsed.data.youtubeUrl,
    },
    file instanceof File && file.size > 0 ? file : undefined,
  );
  if (error !== undefined) return { success: false, message: error };

  revalidatePath(SHOP_PATH);
  return { success: true, id: data };
}

export async function updateContentBoardAction(
  shopId: number,
  contentBoardId: number,
  formData: FormData,
): Promise<ActionResult> {
  const contentType = formData.get("contentType");
  const topic = formData.get("topic");
  const description = formData.get("description");
  const youtubeUrl = formData.get("youtubeUrl");
  const file = formData.get("file");
  const hasExistingFile = formData.get("hasExistingFile") === "true";

  const parsed = contentBoardSchema.safeParse({
    contentType,
    topic,
    description,
    youtubeUrl: youtubeUrl ? String(youtubeUrl) : undefined,
    hasExistingFile: hasExistingFile || (file instanceof File && file.size > 0),
    // FormData 값은 신뢰할 수 없는 문자열이므로 타입 단정 없이 스키마 검증에 그대로 넘긴다.
  });
  if (!parsed.success) return invalidInput(parsed.error.issues[0]?.message);

  const { error } = await shopRepository.updateContentBoard(
    shopId,
    contentBoardId,
    {
      contentType: parsed.data.contentType,
      topic: parsed.data.topic,
      description: parsed.data.description,
      youtubeUrl: parsed.data.youtubeUrl,
    },
    file instanceof File && file.size > 0 ? file : undefined,
  );
  if (error !== undefined) return { success: false, message: error };

  revalidatePath(SHOP_PATH);
  return { success: true };
}

export async function deleteContentBoardAction(shopId: number, contentBoardId: number): Promise<ActionResult> {
  const { error } = await shopRepository.deleteContentBoard(shopId, contentBoardId);
  if (error !== undefined) return { success: false, message: error };

  revalidatePath(SHOP_PATH);
  return { success: true };
}

export async function createPhoneNumberAction(shopId: number, values: PhoneNumberFormValues): Promise<ActionResult> {
  const parsed = phoneNumberSchema.safeParse(values);
  if (!parsed.success) return invalidInput(parsed.error.issues[0]?.message);

  // 등록 직전 서버에서 최신 전화번호 건수를 다시 조회해 최대 건수를 재검증한다.
  // 클라이언트의 로컬 상태만으로는 다른 탭/동시 요청에서의 변경을 감지할 수 없기 때문이다.
  const existing = await shopRepository.getPhoneNumbers(shopId);
  if (existing.error !== undefined) return { success: false, message: existing.error };
  if ((existing.data?.length ?? 0) >= PHONE_NUMBER_MAX_COUNT) {
    return { success: false, message: SHOP_MESSAGE.PHONE_NUMBER_MAX_REACHED };
  }

  const { error, data } = await shopRepository.createPhoneNumber(shopId, parsed.data);
  if (error !== undefined) return { success: false, message: error };

  revalidatePath(SHOP_PATH);
  return { success: true, id: data };
}

export async function setPrimaryPhoneNumberAction(phoneNumberId: number): Promise<ActionResult> {
  const { error } = await shopRepository.setPrimaryPhoneNumber(phoneNumberId);
  if (error !== undefined) return { success: false, message: error };

  revalidatePath(SHOP_PATH);
  return { success: true };
}

export async function deletePhoneNumberAction(phoneNumberId: number): Promise<ActionResult> {
  const { error } = await shopRepository.deletePhoneNumber(phoneNumberId);
  if (error !== undefined) return { success: false, message: error };

  revalidatePath(SHOP_PATH);
  return { success: true };
}

export async function updateShopStatusAction(shopId: number, values: ShopStatusFormValues): Promise<ActionResult> {
  const parsed = shopStatusSchema.safeParse(values);
  if (!parsed.success) return invalidInput(parsed.error.issues[0]?.message);

  const { error } = await shopRepository.updateStatus(shopId, parsed.data);
  if (error !== undefined) return { success: false, message: error };

  revalidatePath(SHOP_PATH);
  revalidatePath(SHOP_STATUS_PATH);
  return { success: true };
}

export async function updateShopMinOrderAmountAction(
  shopId: number,
  values: ShopMinOrderAmountFormValues,
): Promise<ActionResult> {
  const parsed = shopMinOrderAmountSchema.safeParse(values);
  if (!parsed.success) return invalidInput(parsed.error.issues[0]?.message);

  const { error } = await shopRepository.updateMinOrderAmount(shopId, parsed.data);
  if (error !== undefined) return { success: false, message: error };

  revalidatePath(SHOP_PATH);
  return { success: true };
}

// ===== 예약주문 =====

export async function updateShopScheduledOrderAction(
  shopId: number,
  values: ShopScheduledOrderFormValues,
): Promise<ActionResult> {
  const parsed = shopScheduledOrderSchema.safeParse(values);
  if (!parsed.success) return invalidInput(parsed.error.issues[0]?.message);

  const { error } = await shopRepository.updateScheduledOrder(shopId, parsed.data);
  if (error !== undefined) return { success: false, message: error };

  revalidatePath(SHOP_PATH);
  return { success: true };
}

// ===== 배달팁 =====
// 컬렉션은 서버가 replace-all PUT 으로만 받으므로, 시트의 섹션별 저장 버튼이 해당 PUT 하나에 대응한다.

export async function getDeliveryTipsAction(shopId: number): Promise<DataResult<ShopDeliveryTipSetting>> {
  const { data, error } = await shopRepository.getDeliveryTips(shopId);
  if (error !== undefined) return { success: false, message: error };
  if (!data) return { success: false, message: SHOP_MESSAGE.CREATE_UPDATE_FAILED };

  return {
    success: true,
    data: {
      tiers: data.tiers.map((item) => ({
        id: item.id,
        tierOrder: item.tierOrder,
        minOrderAmount: item.minOrderAmount,
        tipAmount: item.tipAmount,
      })),
      extraTipType: data.extraTipType,
      distance: data.distance
        ? {
            baseDistanceMeters: data.distance.baseDistanceMeters,
            surchargeUnit: data.distance.surchargeUnit,
            surchargeAmount: data.distance.surchargeAmount,
          }
        : null,
      regions: data.regions.map((item) => ({
        id: item.id,
        adminDongId: item.adminDongId,
        regionName: item.regionName,
        tipAmount: item.tipAmount,
      })),
      schedules: data.schedules.map((item) => ({
        id: item.id,
        dayType: item.dayType,
        startTime: item.startTime,
        endTime: item.endTime,
        tipAmount: item.tipAmount,
      })),
      holidayTipAmount: data.holidayTipAmount,
    },
  };
}

export async function updateDeliveryTipTiersAction(
  shopId: number,
  values: DeliveryTipTiersFormValues,
): Promise<ActionResult> {
  const parsed = deliveryTipTiersSchema.safeParse(values);
  if (!parsed.success) return invalidInput(parsed.error.issues[0]?.message);

  const { error } = await shopRepository.updateDeliveryTipTiers(shopId, {
    tiers: parsed.data.tiers.map((tier) => ({ minOrderAmount: tier.minOrderAmount, tipAmount: tier.tipAmount })),
  });
  if (error !== undefined) return { success: false, message: error };

  revalidatePath(SHOP_PATH);
  return { success: true };
}

export async function updateDeliveryTipDistanceAction(
  shopId: number,
  values: DeliveryTipDistanceFormValues,
): Promise<ActionResult> {
  const parsed = deliveryTipDistanceSchema.safeParse(values);
  if (!parsed.success) return invalidInput(parsed.error.issues[0]?.message);

  // 거리별을 저장하면 서버가 지역별을 자동 해제한다 — 프론트가 먼저 DELETE 를 호출하지 않는다.
  const { error } = await shopRepository.updateDeliveryTipDistance(shopId, {
    baseDistanceMeters: parsed.data.baseDistanceMeters,
    surchargeUnit: parsed.data.surchargeUnit,
    surchargeAmount: parsed.data.surchargeAmount,
  });
  if (error !== undefined) return { success: false, message: error };

  revalidatePath(SHOP_PATH);
  return { success: true };
}

export async function deleteDeliveryTipDistanceAction(shopId: number): Promise<ActionResult> {
  const { error } = await shopRepository.deleteDeliveryTipDistance(shopId);
  if (error !== undefined) return { success: false, message: error };

  revalidatePath(SHOP_PATH);
  return { success: true };
}

export async function updateDeliveryTipRegionsAction(
  shopId: number,
  values: DeliveryTipRegionsFormValues,
): Promise<ActionResult> {
  const parsed = deliveryTipRegionsSchema.safeParse(values);
  if (!parsed.success) return invalidInput(parsed.error.issues[0]?.message);

  // 지역별을 저장하면 서버가 거리별을 자동 해제한다.
  const { error } = await shopRepository.updateDeliveryTipRegions(shopId, {
    regions: parsed.data.regions.map((region) => ({
      adminDongId: region.adminDongId,
      tipAmount: region.tipAmount,
    })),
  });
  if (error !== undefined) return { success: false, message: error };

  revalidatePath(SHOP_PATH);
  return { success: true };
}

export async function deleteDeliveryTipRegionsAction(shopId: number): Promise<ActionResult> {
  const { error } = await shopRepository.deleteDeliveryTipRegions(shopId);
  if (error !== undefined) return { success: false, message: error };

  revalidatePath(SHOP_PATH);
  return { success: true };
}

export async function updateDeliveryTipSchedulesAction(
  shopId: number,
  values: DeliveryTipSchedulesFormValues,
): Promise<ActionResult> {
  const parsed = deliveryTipSchedulesSchema.safeParse(values);
  if (!parsed.success) return invalidInput(parsed.error.issues[0]?.message);

  const { error } = await shopRepository.updateDeliveryTipSchedules(shopId, {
    schedules: parsed.data.schedules.map((schedule) => ({
      dayType: schedule.dayType,
      startTime: schedule.startTime,
      endTime: schedule.endTime,
      tipAmount: schedule.tipAmount,
    })),
  });
  if (error !== undefined) return { success: false, message: error };

  revalidatePath(SHOP_PATH);
  return { success: true };
}

export async function updateDeliveryTipHolidayAction(
  shopId: number,
  values: DeliveryTipHolidayFormValues,
): Promise<ActionResult> {
  const parsed = deliveryTipHolidaySchema.safeParse(values);
  if (!parsed.success) return invalidInput(parsed.error.issues[0]?.message);

  const { error } = await shopRepository.updateDeliveryTipHoliday(shopId, { tipAmount: parsed.data.tipAmount });
  if (error !== undefined) return { success: false, message: error };

  revalidatePath(SHOP_PATH);
  return { success: true };
}

// ===== 배달가능지역 =====

export async function getDeliveryAreasAction(shopId: number): Promise<DataResult<ShopDeliveryArea[]>> {
  const { data, error } = await shopRepository.getDeliveryAreas(shopId);
  if (error !== undefined) return { success: false, message: error };

  return {
    success: true,
    data: (data ?? []).map((item) => ({
      id: item.id,
      adminDongId: item.adminDongId,
      regionName: item.regionName,
    })),
  };
}

export async function createDeliveryAreaAction(shopId: number, adminDongId: number): Promise<ActionResult> {
  const { data, error } = await shopRepository.createDeliveryArea(shopId, { adminDongId });
  if (error !== undefined) return { success: false, message: error };

  revalidatePath(SHOP_PATH);
  return { success: true, id: data ?? undefined };
}

export async function deleteDeliveryAreaAction(deliveryAreaId: number): Promise<ActionResult> {
  const { error } = await shopRepository.deleteDeliveryArea(deliveryAreaId);
  if (error !== undefined) return { success: false, message: error };

  revalidatePath(SHOP_PATH);
  return { success: true };
}

export async function updateConvenienceInfoAction(
  shopId: number,
  values: ConvenienceInfoFormValues,
): Promise<ActionResult> {
  const parsed = convenienceInfoSchema.safeParse(values);
  if (!parsed.success) return invalidInput(parsed.error.issues[0]?.message);

  const { error } = await shopRepository.updateConvenienceInfo(shopId, parsed.data);
  if (error !== undefined) return { success: false, message: error };

  revalidatePath(SHOP_PATH);
  return { success: true };
}

export async function createAmenityAction(shopId: number, amenityCategoryId: number): Promise<ActionResult> {
  const { error } = await shopRepository.createAmenity(shopId, { amenityCategoryId });
  if (error !== undefined) return { success: false, message: error };

  revalidatePath(SHOP_PATH);
  return { success: true };
}

export async function deleteAmenityAction(shopId: number, amenityCategoryId: number): Promise<ActionResult> {
  const { error } = await shopRepository.deleteAmenity(shopId, amenityCategoryId);
  if (error !== undefined) return { success: false, message: error };

  revalidatePath(SHOP_PATH);
  return { success: true };
}

// ===== 운영정보 =====

export async function createBusinessHourAction(shopId: number, values: BusinessHourValues): Promise<ActionResult> {
  const parsed = businessHourSchema.safeParse(values);
  if (!parsed.success) return invalidInput(parsed.error.issues[0]?.message);

  const { error, data } = await shopRepository.createBusinessHour(shopId, parsed.data);
  if (error !== undefined) return { success: false, message: error };

  revalidatePath(SHOP_PATH);
  return { success: true, id: data };
}

export async function updateBusinessHourAction(
  businessHourId: number,
  values: BusinessHourValues,
): Promise<ActionResult> {
  const parsed = businessHourSchema.safeParse(values);
  if (!parsed.success) return invalidInput(parsed.error.issues[0]?.message);

  const { error } = await shopRepository.updateBusinessHour(businessHourId, parsed.data);
  if (error !== undefined) return { success: false, message: error };

  revalidatePath(SHOP_PATH);
  return { success: true };
}

export async function createBreakTimeAction(shopId: number, values: DayTimeRangeValues): Promise<ActionResult> {
  const parsed = dayTimeRangeSchema.safeParse(values);
  if (!parsed.success) return invalidInput(parsed.error.issues[0]?.message);

  const { error, data } = await shopRepository.createBreakTime(shopId, parsed.data);
  if (error !== undefined) return { success: false, message: error };

  revalidatePath(SHOP_PATH);
  return { success: true, id: data };
}

export async function updateBreakTimeAction(breakTimeId: number, values: DayTimeRangeValues): Promise<ActionResult> {
  const parsed = dayTimeRangeSchema.safeParse(values);
  if (!parsed.success) return invalidInput(parsed.error.issues[0]?.message);

  const { error } = await shopRepository.updateBreakTime(breakTimeId, parsed.data);
  if (error !== undefined) return { success: false, message: error };

  revalidatePath(SHOP_PATH);
  return { success: true };
}

export async function deleteBreakTimeAction(breakTimeId: number): Promise<ActionResult> {
  const { error } = await shopRepository.deleteBreakTime(breakTimeId);
  if (error !== undefined) return { success: false, message: error };

  revalidatePath(SHOP_PATH);
  return { success: true };
}

export async function updateHolidayClosedAction(
  shopId: number,
  values: HolidayClosedFormValues,
): Promise<ActionResult> {
  const parsed = holidayClosedSchema.safeParse(values);
  if (!parsed.success) return invalidInput(parsed.error.issues[0]?.message);

  const { error } = await shopRepository.updateHolidayClosed(shopId, parsed.data);
  if (error !== undefined) return { success: false, message: error };

  revalidatePath(SHOP_PATH);
  return { success: true };
}

export async function createClosedDayAction(shopId: number, values: ClosedDayFormValues): Promise<ActionResult> {
  const parsed = closedDaySchema.safeParse(values);
  if (!parsed.success) return invalidInput(parsed.error.issues[0]?.message);

  // 등록 직전 서버에서 최신 정기휴무 건수를 다시 조회해 최대 건수를 재검증한다.
  const existing = await shopRepository.getClosedDays(shopId);
  if (existing.error !== undefined) return { success: false, message: existing.error };
  if ((existing.data?.regularClosedDays.length ?? 0) >= REGULAR_CLOSED_DAY_MAX_COUNT) {
    return { success: false, message: SHOP_MESSAGE.REGULAR_CLOSED_DAY_MAX_REACHED };
  }

  const { error, data } = await shopRepository.createClosedDay(shopId, parsed.data);
  if (error !== undefined) return { success: false, message: error };

  revalidatePath(SHOP_PATH);
  return { success: true, id: data };
}

export async function deleteClosedDayAction(closedDayId: number): Promise<ActionResult> {
  const { error } = await shopRepository.deleteClosedDay(closedDayId);
  if (error !== undefined) return { success: false, message: error };

  revalidatePath(SHOP_PATH);
  return { success: true };
}

export async function createTemporaryClosureAction(
  shopId: number,
  values: TemporaryClosureFormValues,
): Promise<ActionResult> {
  const parsed = temporaryClosureSchema.safeParse(values);
  if (!parsed.success) return invalidInput(parsed.error.issues[0]?.message);

  const { error, data } = await shopRepository.createTemporaryClosure(shopId, parsed.data);
  if (error !== undefined) return { success: false, message: error };

  revalidatePath(SHOP_PATH);
  return { success: true, id: data };
}

export async function deleteTemporaryClosureAction(temporaryClosureId: number): Promise<ActionResult> {
  const { error } = await shopRepository.deleteTemporaryClosure(temporaryClosureId);
  if (error !== undefined) return { success: false, message: error };

  revalidatePath(SHOP_PATH);
  return { success: true };
}

// ===== 전체현황 · 임시중지 =====

export async function createSuspensionAction(values: SuspensionFormValues): Promise<ActionResult> {
  const parsed = suspensionSchema.safeParse(values);
  if (!parsed.success) return invalidInput(parsed.error.issues[0]?.message);

  // 스펙은 startAt/endAt 을 LocalDateTime("YYYY-MM-DDTHH:mm:ss", 오프셋 없음)으로 요구한다.
  const payload = {
    reason: parsed.data.reason,
    orderMethods: parsed.data.orderMethods,
    startAt: toLocalDateTimeString(parsed.data.startAt),
    endAt: toLocalDateTimeString(parsed.data.endAt),
  };

  if (parsed.data.shopIds.length === 1) {
    const { error } = await shopRepository.createSuspension(parsed.data.shopIds[0], payload);
    if (error !== undefined) return { success: false, message: error };
  } else {
    const { error } = await shopRepository.createSuspensionsBulk({ shopIds: parsed.data.shopIds, ...payload });
    if (error !== undefined) return { success: false, message: error };
  }

  revalidatePath(SHOP_STATUS_PATH);
  return { success: true };
}

export async function releaseSuspensionAction(shopId: number, suspensionId: number): Promise<ActionResult> {
  const { error } = await shopRepository.releaseSuspension(shopId, suspensionId);
  if (error !== undefined) return { success: false, message: error };

  revalidatePath(SHOP_STATUS_PATH);
  return { success: true };
}

// ===== 라이더 가게방문 안내 · 픽업 위치 =====

export async function validateShopRiderVisitGuideAction(
  shopId: number,
  values: ShopRiderVisitGuideFormValues,
): Promise<DataResult<{ valid: boolean; violations: string[] }>> {
  const parsed = shopRiderVisitGuideSchema.safeParse(values);
  if (!parsed.success) return { success: false, message: parsed.error.issues[0]?.message };

  const { error, data } = await shopRepository.validateRiderVisitGuide(shopId, parsed.data);
  if (error !== undefined || data === undefined) return { success: false, message: error };

  return { success: true, data };
}

// 빈 문자열을 그대로 보내면 서버가 문구를 삭제한다 — 삭제 전용 액션을 따로 두지 않는다.
export async function updateShopRiderVisitGuideAction(
  shopId: number,
  values: ShopRiderVisitGuideFormValues,
): Promise<ActionResult> {
  const parsed = shopRiderVisitGuideSchema.safeParse(values);
  if (!parsed.success) return invalidInput(parsed.error.issues[0]?.message);

  const { error } = await shopRepository.updateRiderVisitGuide(shopId, parsed.data);
  if (error !== undefined) return { success: false, message: error };

  revalidatePath(SHOP_PATH);
  return { success: true };
}

export async function updateShopRiderPickupLocationAction(
  shopId: number,
  values: ShopRiderPickupLocationFormValues,
): Promise<ActionResult> {
  const parsed = shopRiderPickupLocationSchema.safeParse(values);
  if (!parsed.success) return invalidInput(parsed.error.issues[0]?.message);

  const { detailAddress, latitude, longitude, lotAddress, roadAddress } = parsed.data;
  const { error } = await shopRepository.updateRiderPickupLocation(shopId, {
    roadAddress,
    // 선택 입력은 빈 문자열 대신 null 로 보내, 서버의 '전부 채우거나 전부 비우거나' 판정과 어긋나지 않게 한다.
    lotAddress: lotAddress.length > 0 ? lotAddress : null,
    detailAddress: detailAddress.length > 0 ? detailAddress : null,
    latitude,
    longitude,
  });
  if (error !== undefined) return { success: false, message: error };

  revalidatePath(SHOP_PATH);
  return { success: true };
}

export async function clearShopRiderPickupLocationAction(shopId: number): Promise<ActionResult> {
  const { error } = await shopRepository.clearRiderPickupLocation(shopId);
  if (error !== undefined) return { success: false, message: error };

  revalidatePath(SHOP_PATH);
  return { success: true };
}
