"use server";

import { revalidatePath } from "next/cache";

import { ALLOWED_IMAGE_TYPES, MAX_IMAGE_SIZE_BYTES } from "@/api/file/file.dto";
import { regionRepository } from "@/api/region/region.repository";
import { regionService } from "@/api/region/region.service";
import type {
  GeoPointResponse,
  ShopDeliveryAreaBulkResponse,
  ShopDeliveryAreaPolygonCandidateResponse,
} from "@/api/shop/shop.dto";
import { shopRepository } from "@/api/shop/shop.repository";
import { shopService } from "@/api/shop/shop.service";
import { shopRequestRepository } from "@/api/shop-request/shop-request.repository";
import type {
  AdminDong,
  AdminDongBoundaryResult,
  AdminDongTree,
  AmenityCategory,
  BusinessHour,
  ContentBoardItem,
  DeliveryAreaAdjustmentRequest,
  DeliveryAreaBulkOutcome,
  DeliveryAreaPolygon,
  DeliveryAreaPolygonCandidate,
  DeliveryAreaPolygonPreview,
  DeliveryAreaRadiusPreview,
  GeoPoint,
  MenuCollectionImage,
  PhoneNumber,
  ShopDeliveryArea,
  ShopDeliveryTipSetting,
  ShopOperationInfo,
  ShopOrderNotice,
  ShopSummary,
  Suspension,
} from "@/feature/shop/domain";

import {
  ADMIN_DONG_SEARCH_SIZE,
  ALLOWED_CONSENT_TYPES,
  CONTENT_BOARD_MAX_COUNT,
  MENU_COLLECTION_MAX_COUNT,
  PHONE_NUMBER_MAX_COUNT,
  REGULAR_CLOSED_DAY_MAX_COUNT,
} from "./constants";
import { SHOP_MENU_COLLECTION_MESSAGE, SHOP_MESSAGE, SHOP_REQUEST_COPY } from "./message";
import {
  type AdminDongBoundaryFormValues,
  adminDongBoundarySchema,
  type BusinessHourValues,
  businessHourSchema,
  type ClosedDayFormValues,
  CONTENT_BOARD_LIMIT_MESSAGE,
  type ConvenienceInfoFormValues,
  closedDaySchema,
  contentBoardSchema,
  convenienceInfoSchema,
  type DayTimeRangeValues,
  type DeliveryAreaBulkFormValues,
  type DeliveryAreaCreateFormValues,
  type DeliveryAreaPolygonFormValues,
  type DeliveryAreaRadiusFormValues,
  type DeliveryTipDistanceFormValues,
  type DeliveryTipHolidayFormValues,
  type DeliveryTipRegionsFormValues,
  type DeliveryTipSchedulesFormValues,
  type DeliveryTipTiersFormValues,
  dayTimeRangeSchema,
  deliveryAreaAdjustmentSchema,
  deliveryAreaBulkSchema,
  deliveryAreaCreateSchema,
  deliveryAreaPolygonSchema,
  deliveryAreaRadiusSchema,
  deliveryTipDistanceSchema,
  deliveryTipHolidaySchema,
  deliveryTipRegionsSchema,
  deliveryTipSchedulesSchema,
  deliveryTipTiersSchema,
  type HolidayClosedFormValues,
  holidayClosedSchema,
  orderNoticeSchema,
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
const SHOP_REQUEST_PATH = "/dashboard/shop/requests";

/** 취소 불가(대기중이 아님) 409 — 이 코드만 전용 문구로 갈라 낸다 */
const SHOP_REQUEST_NOT_CANCELABLE_CODE = "SHOP_REQUEST_NOT_CANCELABLE";
/** 서버 `@Size(max = 1000)` 과 같은 값 */
const SHOP_REQUEST_COMMENT_MAX = 1000;

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

/**
 * 정보제공 동의서 첨부 검증.
 *
 * 스캔본 이미지와 PDF 를 모두 받으므로 `extractFile` 의 `ALLOWED_IMAGE_TYPES` 를 쓸 수 없다.
 * 치수 검증은 하지 않는다 — PDF 는 `createImageBitmap` 으로 열리지 않는다.
 */
function extractConsentFile(formData: FormData): { file: File } | { error: string } {
  const file = formData.get("file");
  if (!assertUploadableImage(file)) return { error: SHOP_MESSAGE.CONSENT_FILE_REQUIRED };
  if (!ALLOWED_CONSENT_TYPES.includes(file.type as (typeof ALLOWED_CONSENT_TYPES)[number])) {
    return { error: SHOP_MESSAGE.CONSENT_FILE_TYPE };
  }
  if (file.size > MAX_IMAGE_SIZE_BYTES) return { error: SHOP_MESSAGE.CONSENT_FILE_SIZE };
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

/** 화면은 km 로 다루고 서버는 m 로 받는다. 0.5km 단위라 소수 오차가 남지 않도록 반올림한다 */
function toRadiusMeters(radiusKm: number): number {
  return Math.round(radiusKm * 1000);
}

/** 서버가 좌표를 문자열(BigDecimal 직렬화)로 줄 수도 있어 숫자로 통일한다 */
function toGeoPoint(point: GeoPointResponse): GeoPoint {
  return { latitude: Number(point.latitude), longitude: Number(point.longitude) };
}

function toPolygonCandidate(item: ShopDeliveryAreaPolygonCandidateResponse): DeliveryAreaPolygonCandidate {
  return {
    adminDongId: item.adminDongId,
    regionName: item.regionName,
    alreadyRegistered: item.alreadyRegistered,
  };
}

/**
 * 일괄 처리 응답을 화면이 쓰는 단일 형태로 맞춘다.
 *
 * 추가 응답에는 `removedCount` 가, 삭제 응답에는 `added`/`skipped` 가 없으므로 없는 쪽을 0 으로 채운다.
 */
function toBulkOutcome(data: ShopDeliveryAreaBulkResponse | undefined): DeliveryAreaBulkOutcome {
  return {
    requestedCount: data?.requestedCount ?? 0,
    addedCount: data?.addedCount ?? 0,
    skippedCount: data?.skippedCount ?? 0,
    removedCount: data?.removedCount ?? 0,
    totalCount: data?.totalCount ?? 0,
  };
}

export async function getDeliveryAreasAction(shopId: number): Promise<DataResult<ShopDeliveryArea[]>> {
  const { data, error } = await shopRepository.getDeliveryAreas(shopId);
  if (error !== undefined) return { success: false, message: error };

  return {
    success: true,
    data: (data ?? []).map((item) => ({
      id: item.id,
      adminDongId: item.adminDongId,
      regionName: item.regionName,
      // 구버전 백엔드는 source 를 내려주지 않는다. 그 시절 행은 전부 직접 등록분이므로 MANUAL 로 본다.
      source: item.source ?? "MANUAL",
    })),
  };
}

export async function createDeliveryAreaAction(
  shopId: number,
  values: DeliveryAreaCreateFormValues,
): Promise<ActionResult> {
  const parsed = deliveryAreaCreateSchema.safeParse(values);
  if (!parsed.success) return invalidInput(parsed.error.issues[0]?.message);

  const { data, error } = await shopRepository.createDeliveryArea(shopId, { adminDongId: parsed.data.adminDongId });
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

/** 배달가능지역으로 등록할 행정동 검색 — 조회이므로 DataResult 를 반환한다 */
export async function searchAdminDongsAction(
  keyword: string,
  page = 0,
  size = ADMIN_DONG_SEARCH_SIZE,
): Promise<DataResult<AdminDong[]>> {
  const { data, error } = await regionRepository.searchAdminDongs({ keyword }, { page, size });
  if (error !== undefined) return { success: false, message: error };

  return {
    success: true,
    data: (data ?? []).map((item) => ({
      id: item.id,
      code: item.code,
      regionName: item.regionName,
    })),
  };
}

// ===== 배달가능지역 일괄 처리 =====

/**
 * 행정동 일괄 추가.
 *
 * 이미 등록된 동은 서버가 실패시키지 않고 건너뛴다(`skippedCount`). 중복 1건으로 전체를
 * 실패시키면 "반경 추가 → 다시 반경 추가"가 항상 실패하기 때문이다.
 */
export async function addDeliveryAreasAction(
  shopId: number,
  values: DeliveryAreaBulkFormValues,
): Promise<DataResult<DeliveryAreaBulkOutcome>> {
  const parsed = deliveryAreaBulkSchema.safeParse(values);
  if (!parsed.success) return invalidInput(parsed.error.issues[0]?.message);

  const { data, error } = await shopRepository.addDeliveryAreas(shopId, { adminDongIds: parsed.data.adminDongIds });
  if (error !== undefined) return { success: false, message: error };

  revalidatePath(SHOP_PATH);
  return { success: true, data: toBulkOutcome(data) };
}

/**
 * 행정동 일괄 삭제.
 *
 * 지역별 배달팁이 걸린 동이 하나라도 섞이면 서버가 한 건도 지우지 않고 409 를 낸다.
 * 부분 성공을 허용하면 점주가 무엇이 지워졌는지 알 수 없기 때문이며, 막힌 동 이름이
 * 에러 메시지에 담겨 오므로 그대로 노출한다.
 */
export async function removeDeliveryAreasAction(
  shopId: number,
  values: DeliveryAreaBulkFormValues,
): Promise<DataResult<DeliveryAreaBulkOutcome>> {
  const parsed = deliveryAreaBulkSchema.safeParse(values);
  if (!parsed.success) return invalidInput(parsed.error.issues[0]?.message);

  const { data, error } = await shopRepository.removeDeliveryAreas(shopId, { adminDongIds: parsed.data.adminDongIds });
  if (error !== undefined) return { success: false, message: error };

  revalidatePath(SHOP_PATH);
  return { success: true, data: toBulkOutcome(data) };
}

// ===== 배달가능지역 반경 설정 =====

/** 반경 미리보기 — 저장하지 않으므로 revalidate 하지 않는다 */
export async function previewDeliveryAreaRadiusAction(
  shopId: number,
  values: DeliveryAreaRadiusFormValues,
): Promise<DataResult<DeliveryAreaRadiusPreview>> {
  const parsed = deliveryAreaRadiusSchema.safeParse(values);
  if (!parsed.success) return invalidInput(parsed.error.issues[0]?.message);

  const { data, error } = await shopRepository.getDeliveryAreaRadiusPreview(shopId, {
    radiusMeters: toRadiusMeters(parsed.data.radiusKm),
  });
  if (error !== undefined) return { success: false, message: error };
  if (!data) return { success: false, message: SHOP_MESSAGE.DELIVERY_AREA_LOAD_FAILED };

  return {
    success: true,
    data: {
      centerLatitude: Number(data.centerLatitude),
      centerLongitude: Number(data.centerLongitude),
      radiusMeters: data.radiusMeters,
      maxAllowedRadiusMeters: data.maxAllowedRadiusMeters,
      defaultExposureRadiusMeters: data.defaultExposureRadiusMeters,
      circle: (data.circle ?? []).map(toGeoPoint),
      adminDongs: (data.adminDongs ?? []).map((item) => ({
        adminDongId: item.adminDongId,
        regionName: item.regionName,
        centerLatitude: Number(item.centerLatitude),
        centerLongitude: Number(item.centerLongitude),
        alreadyRegistered: item.alreadyRegistered,
      })),
      adminDongCount: data.adminDongCount,
      unresolvedCount: data.unresolvedCount,
    },
  };
}

/** 반경 확정 적용 — `replace: true` 면 기존 직접 등록분을 교체한다 */
export async function applyDeliveryAreaRadiusAction(
  shopId: number,
  values: DeliveryAreaRadiusFormValues,
): Promise<DataResult<DeliveryAreaBulkOutcome>> {
  const parsed = deliveryAreaRadiusSchema.safeParse(values);
  if (!parsed.success) return invalidInput(parsed.error.issues[0]?.message);

  const { data, error } = await shopRepository.applyDeliveryAreaRadius(shopId, {
    radiusMeters: toRadiusMeters(parsed.data.radiusKm),
    replace: parsed.data.replace,
  });
  if (error !== undefined) return { success: false, message: error };

  revalidatePath(SHOP_PATH);
  return { success: true, data: toBulkOutcome(data) };
}

// ===== 배달지역 도형 =====

/** 저장된 도형 조회. 미설정은 오류가 아니므로 `exists: false` 를 그대로 통과시킨다 */
export async function fetchDeliveryAreaPolygonAction(shopId: number): Promise<DataResult<DeliveryAreaPolygon>> {
  const { data, error } = await shopRepository.getDeliveryAreaPolygon(shopId);
  if (error !== undefined) return { success: false, message: error };
  if (!data) return { success: false, message: SHOP_MESSAGE.DELIVERY_AREA_LOAD_FAILED };

  return {
    success: true,
    data: {
      exists: data.exists,
      rings: data.rings ? data.rings.map((ring) => ring.map(toGeoPoint)) : null,
      centerLatitude: data.centerLatitude === null ? null : Number(data.centerLatitude),
      centerLongitude: data.centerLongitude === null ? null : Number(data.centerLongitude),
      shopLatitude: Number(data.shopLatitude),
      shopLongitude: Number(data.shopLongitude),
      centerMovedMeters: data.centerMovedMeters,
      maxRadiusMeters: data.maxRadiusMeters,
      maxAllowedRadiusMeters: data.maxAllowedRadiusMeters,
      defaultExposureRadiusMeters: data.defaultExposureRadiusMeters,
      ringCount: data.ringCount,
      vertexCount: data.vertexCount,
      projectedAdminDongCount: data.projectedAdminDongCount,
      updatedAt: data.updatedAt,
    },
  };
}

/**
 * 도형 환산 미리보기.
 *
 * 저장 직전에 호출해 `blockedAdminDongs`(배달팁이 걸려 닫을 수 없는 동)를 먼저 보여주면
 * 점주가 저장에서 409 를 맞기 전에 배달팁을 정리할 수 있다.
 */
export async function previewDeliveryAreaPolygonAction(
  shopId: number,
  values: DeliveryAreaPolygonFormValues,
): Promise<DataResult<DeliveryAreaPolygonPreview>> {
  const parsed = deliveryAreaPolygonSchema.safeParse(values);
  if (!parsed.success) return invalidInput(parsed.error.issues[0]?.message);

  const { data, error } = await shopRepository.previewDeliveryAreaPolygon(shopId, { rings: parsed.data.rings });
  if (error !== undefined) return { success: false, message: error };
  if (!data) return { success: false, message: SHOP_MESSAGE.DELIVERY_AREA_LOAD_FAILED };

  return {
    success: true,
    data: {
      maxRadiusMeters: data.maxRadiusMeters,
      withinAllowedRadius: data.withinAllowedRadius,
      adminDongs: (data.adminDongs ?? []).map(toPolygonCandidate),
      addedAdminDongs: (data.addedAdminDongs ?? []).map(toPolygonCandidate),
      removedAdminDongs: (data.removedAdminDongs ?? []).map(toPolygonCandidate),
      blockedAdminDongs: (data.blockedAdminDongs ?? []).map((item) => ({
        adminDongId: item.adminDongId,
        regionName: item.regionName,
        reason: item.reason,
      })),
      unresolvedCount: data.unresolvedCount,
    },
  };
}

/**
 * 도형 저장(전체 교체).
 *
 * 응답 본문이 없는 명령이므로 호출부는 성공 후 도형·목록을 다시 조회한다.
 */
export async function saveDeliveryAreaPolygonAction(
  shopId: number,
  values: DeliveryAreaPolygonFormValues,
): Promise<ActionResult> {
  const parsed = deliveryAreaPolygonSchema.safeParse(values);
  if (!parsed.success) return invalidInput(parsed.error.issues[0]?.message);

  const { error } = await shopRepository.saveDeliveryAreaPolygon(shopId, { rings: parsed.data.rings });
  if (error !== undefined) return { success: false, message: error };

  revalidatePath(SHOP_PATH);
  return { success: true };
}

/** 도형 해제 — 환산된 행정동만 지우고 직접 등록한 행정동은 남는다 */
export async function deleteDeliveryAreaPolygonAction(shopId: number): Promise<ActionResult> {
  const { error } = await shopRepository.deleteDeliveryAreaPolygon(shopId);
  if (error !== undefined) return { success: false, message: error };

  revalidatePath(SHOP_PATH);
  return { success: true };
}

// ===== 행정동 계층 · 경계 =====

/**
 * 행정동 계층 3단 lazy 조회.
 *
 * 인자가 없으면 시도, `sidoName` 만 주면 시군구, 둘 다 주면 행정동 목록을 받는다.
 * `regionName` 을 공백으로 잘라 클라이언트가 트리를 조립하는 우회는 쓰지 않는다 —
 * 동명에 공백이 들어가면 깨진다.
 */
export async function fetchAdminDongTreeAction(
  sidoName?: string,
  sigunguName?: string,
): Promise<DataResult<AdminDongTree>> {
  const { data, error } = await regionService.getAdminDongTree({ sidoName, sigunguName });
  if (error !== undefined) return { success: false, message: error };
  if (!data) return { success: false, message: SHOP_MESSAGE.DELIVERY_AREA_TREE_LOAD_FAILED };

  return { success: true, data };
}

/**
 * 뷰포트 경계 조회.
 *
 * bbox 가 너무 넓으면 서버가 오류 대신 `truncated: true` + 빈 목록을 주므로, 호출부는
 * 그 상태를 "확대하면 편집할 수 있습니다" 안내로 처리한다.
 */
export async function fetchAdminDongBoundariesAction(
  bounds: AdminDongBoundaryFormValues,
): Promise<DataResult<AdminDongBoundaryResult>> {
  const parsed = adminDongBoundarySchema.safeParse(bounds);
  if (!parsed.success) return invalidInput(parsed.error.issues[0]?.message);

  const { data, error } = await regionService.getAdminDongBoundaries(parsed.data);
  if (error !== undefined) return { success: false, message: error };
  if (!data) return { success: false, message: SHOP_MESSAGE.DELIVERY_AREA_BOUNDARY_LOAD_FAILED };

  return { success: true, data };
}

// ===== 배달지역 조정 신청 =====

export async function fetchDeliveryAreaAdjustmentsAction(
  shopId: number,
): Promise<DataResult<DeliveryAreaAdjustmentRequest[]>> {
  const { data, error } = await shopRepository.getDeliveryAreaAdjustments(shopId);
  if (error !== undefined) return { success: false, message: error };

  return {
    success: true,
    data: (data ?? []).map((item) => ({
      id: item.id,
      counterpartShopName: item.counterpartShopName,
      counterpartBusinessNumber: item.counterpartBusinessNumber,
      franchiseName: item.franchiseName,
      reason: item.reason,
      consentFileUrl: item.consentFileUrl,
      status: item.status,
      rejectReason: item.rejectReason,
      createdAt: item.createdAt,
    })),
  };
}

/**
 * 조정 신청 접수.
 *
 * 동의서 파일을 함께 보내야 하므로 FormData 를 그대로 받아 multipart 로 패스스루한다.
 * 텍스트 필드는 여기서 Zod 로 다시 검증한 뒤 새 FormData 로 재조립한다.
 */
export async function requestDeliveryAreaAdjustmentAction(shopId: number, formData: FormData): Promise<ActionResult> {
  const parsed = deliveryAreaAdjustmentSchema.safeParse({
    counterpartShopName: formData.get("counterpartShopName"),
    counterpartBusinessNumber: formData.get("counterpartBusinessNumber"),
    franchiseName: formData.get("franchiseName"),
    reason: formData.get("reason"),
  });
  if (!parsed.success) return invalidInput(parsed.error.issues[0]?.message);

  const extracted = extractConsentFile(formData);
  if ("error" in extracted) return { success: false, message: extracted.error };

  const payload = new FormData();
  payload.append("counterpartShopName", parsed.data.counterpartShopName);
  payload.append("counterpartBusinessNumber", parsed.data.counterpartBusinessNumber);
  payload.append("franchiseName", parsed.data.franchiseName);
  payload.append("reason", parsed.data.reason);
  payload.append("file", extracted.file);

  const { data, error } = await shopRepository.createDeliveryAreaAdjustment(shopId, payload);
  if (error !== undefined) return { success: false, message: error };

  revalidatePath(SHOP_PATH);
  return { success: true, id: data ?? undefined };
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

// ===== 요청처리 현황 =====

/**
 * 요청 취소.
 *
 * 취소 가능 조건(PENDING 만)은 서버 애그리거트의 불변식이라 여기서 선판정하지 않고,
 * 409 `SHOP_REQUEST_NOT_CANCELABLE` 만 전용 문구로 갈라 낸다 — 그 외 실패는 일반 문구다.
 */
export async function cancelShopRequestAction(shopId: number, requestId: number): Promise<ActionResult> {
  const { error, errorCode } = await shopRequestRepository.cancel(shopId, requestId);
  if (error !== undefined) {
    return {
      success: false,
      message:
        errorCode === SHOP_REQUEST_NOT_CANCELABLE_CODE
          ? SHOP_REQUEST_COPY.CANCEL_NOT_ALLOWED
          : SHOP_REQUEST_COPY.CANCEL_FAILED,
    };
  }

  revalidatePath(SHOP_REQUEST_PATH);
  return { success: true };
}

/**
 * 요청 문의 작성.
 *
 * 클라이언트 검증만 믿지 않고 여기서도 공백·길이를 확인한다 — 서버 액션은 클라이언트를
 * 거치지 않고도 호출될 수 있으므로, 400 을 맞기 전에 같은 규칙으로 막는다.
 */
export async function createShopRequestCommentAction(
  shopId: number,
  requestId: number,
  content: string,
): Promise<ActionResult> {
  const trimmed = content.trim();
  if (trimmed.length === 0) return { success: false, message: SHOP_REQUEST_COPY.COMMENT_REQUIRED };
  if (trimmed.length > SHOP_REQUEST_COMMENT_MAX) {
    return { success: false, message: SHOP_REQUEST_COPY.COMMENT_MAX_LENGTH };
  }

  const { data, error } = await shopRequestRepository.createComment(shopId, requestId, { content: trimmed });
  if (error !== undefined) return { success: false, message: SHOP_REQUEST_COPY.COMMENT_FAILED };

  revalidatePath(SHOP_REQUEST_PATH);
  return { success: true, id: data ?? undefined };
}

// =====================================================================================
// 메뉴모음컷 · 주문안내 (`docs/tasks/menu-board-promotion/frontend.md` A)
//
// 두 기능 모두 메뉴판 화면(`/dashboard/shop/menus`)의 시트에서만 쓰이므로,
// `SHOP_PATH` 가 아니라 메뉴판 세그먼트를 무효화한다 — 메뉴판은 상세(`[productId]`)가 동적
// 세그먼트라 `layout` 스코프여야 목록과 상세가 함께 갱신된다(`feature/product/actions.ts` 선례).
// =====================================================================================

const MENU_BOARD_PATH = "/dashboard/shop/menus";

function revalidateMenuBoard(): void {
  revalidatePath(MENU_BOARD_PATH, "layout");
}

export async function loadMenuCollectionImagesAction(shopId: number): Promise<DataResult<MenuCollectionImage[]>> {
  const { data, error } = await shopRepository.getMenuCollectionImages(shopId);
  if (error !== undefined) return { success: false, message: error };
  return { success: true, data: data ?? [] };
}

/**
 * 메뉴모음컷 등록 요청.
 *
 * 상한(6개)은 서버도 보지만 화면이 먼저 막는다 — 파일을 다 올려 보낸 뒤 400 을 받으면
 * 업로드 대기 시간이 헛되기 때문이다. 다만 **규격 판정은 서버가 한다**
 * (`..._SPEC_INVALID`) — 브라우저에서 해상도를 재도 서버가 `ImageIO` 로 다시 보므로
 * 두 판정이 어긋날 수 있어 서버 문구를 그대로 노출한다.
 */
export async function requestMenuCollectionImageAction(
  shopId: number,
  currentCount: number,
  formData: FormData,
): Promise<ActionResult> {
  if (currentCount >= MENU_COLLECTION_MAX_COUNT) {
    return { success: false, message: SHOP_MENU_COLLECTION_MESSAGE.LIMIT_EXCEEDED };
  }

  const extracted = extractFile(formData);
  if ("error" in extracted) return { success: false, message: extracted.error };

  const { error } = await shopRepository.requestMenuCollectionImage(shopId, extracted.file);
  if (error !== undefined) return { success: false, message: error };

  revalidateMenuBoard();
  return { success: true };
}

/** 순서 변경은 검수 대상이 아니라 즉시 반영된다. `sort` 를 계산하지 않고 확정된 id 배열만 보낸다 */
export async function changeMenuCollectionImageOrderAction(shopId: number, imageIds: number[]): Promise<ActionResult> {
  if (imageIds.length === 0) return invalidInput();

  const { error } = await shopRepository.changeMenuCollectionImageOrder(shopId, { imageIds });
  if (error !== undefined) return { success: false, message: error };

  revalidateMenuBoard();
  return { success: true };
}

/**
 * 메뉴모음컷 삭제.
 *
 * 최소 1개 규칙은 화면이 버튼을 잠가 막지만, 서버 액션은 클라이언트를 거치지 않고도 호출될 수
 * 있으므로 여기서도 같은 규칙으로 한 번 더 본다(`createShopRequestCommentAction` 선례).
 */
export async function deleteMenuCollectionImageAction(
  shopId: number,
  imageId: number,
  currentCount: number,
): Promise<ActionResult> {
  if (currentCount <= 1) {
    return { success: false, message: SHOP_MENU_COLLECTION_MESSAGE.LAST_CANNOT_DELETE };
  }

  const { error } = await shopRepository.deleteMenuCollectionImage(shopId, imageId);
  if (error !== undefined) return { success: false, message: error };

  revalidateMenuBoard();
  return { success: true };
}

export async function loadOrderNoticeAction(shopId: number): Promise<DataResult<ShopOrderNotice>> {
  const { data, error } = await shopRepository.getOrderNotice(shopId);
  if (error !== undefined || !data) return { success: false, message: error };
  return { success: true, data };
}

/** 검수 없이 즉시 반영된다 — 게시중단은 관리자가 사후에 거는 조치라 저장을 막지 않는다 */
export async function updateOrderNoticeAction(shopId: number, content: string): Promise<ActionResult> {
  const parsed = orderNoticeSchema.safeParse({ content });
  if (!parsed.success) return invalidInput(parsed.error.issues[0]?.message);

  const { error } = await shopRepository.updateOrderNotice(shopId, { content: parsed.data.content });
  if (error !== undefined) return { success: false, message: error };

  revalidateMenuBoard();
  return { success: true };
}
