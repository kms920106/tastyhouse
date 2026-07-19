"use server";

import { revalidatePath } from "next/cache";

import { fileRepository } from "@/api/file/file.repository";
import type { OrderMethod as OrderMethodValue } from "@/api/shop/shop.dto";
import { shopRepository } from "@/api/shop/shop.repository";
import { shopService } from "@/api/shop/shop.service";
import type {
  AmenityCategory,
  BannerImage,
  BreakTime,
  BusinessHour,
  ClosedDay,
  EditorChoice,
  FoodTypeCategory,
  OrderMethod,
  PhotoCategory,
  PhotoImage,
  ShopAmenity,
  ShopDetail,
  ShopFoodType,
  Station,
  Tag,
} from "@/feature/shop/domain";

import { SHOP_MESSAGE } from "./message";
import {
  type AmenityCategoryFormValues,
  amenityCategorySchema,
  type BannerFormValues,
  type BreakTimeFormValues,
  type BusinessHourFormValues,
  bannerSchema,
  breakTimeSchema,
  businessHourSchema,
  type ClosedDayFormValues,
  closedDaySchema,
  type EditorChoiceFormValues,
  editorChoiceSchema,
  type FoodTypeCategoryFormValues,
  foodTypeCategorySchema,
  type OrderMethodFormValues,
  orderMethodSchema,
  type PhotoCategoryFormValues,
  type PhotoImageFormValues,
  type PhotoImageUpdateFormValues,
  photoCategorySchema,
  photoImageSchema,
  photoImageUpdateSchema,
  type ShopAmenityFormValues,
  type ShopFoodTypeFormValues,
  type ShopFormValues,
  shopAmenitySchema,
  shopFoodTypeSchema,
  shopFormSchema,
  type TagFormValues,
  tagSchema,
} from "./schema";

const SHOPS_PATH = "/dashboard/shops";
const shopDetailPath = (id: number) => `${SHOPS_PATH}/${id}`;

type ActionResult = {
  success: boolean;
  message?: string;
  id?: number;
};

type ImageUploadResult = {
  success: boolean;
  message?: string;
  /** 업로드된 파일의 ID(fileId) */
  fileId?: number;
};

async function uploadImage(formData: FormData): Promise<ImageUploadResult> {
  const file = formData.get("file");
  if (!(file instanceof File)) {
    return { success: false, message: SHOP_MESSAGE.IMAGE_UPLOAD_FAILED };
  }

  const { error, data } = await fileRepository.uploadImage(file);
  if (error !== undefined || data == null) {
    return { success: false, message: error ?? SHOP_MESSAGE.IMAGE_UPLOAD_FAILED };
  }

  return { success: true, fileId: data };
}

// 이미지 업로드 (파일 → fileId) — 썸네일/편의시설·음식종류 아이콘/배너/포토 공용
export async function uploadShopImageAction(formData: FormData): Promise<ImageUploadResult> {
  return uploadImage(formData);
}

// ===== Phase A. 가게 본체 CRUD =====

// 지하철역 목록 조회 (등록/수정 폼 드롭다운용)
export async function fetchStationsAction(): Promise<{ success: boolean; message?: string; data?: Station[] }> {
  const { error, data } = await shopService.getStations();
  if (error !== undefined) {
    return { success: false, message: error };
  }
  return { success: true, data };
}

function toShopBody(values: ShopFormValues) {
  return {
    stationId: values.stationId,
    name: values.name,
    latitude: values.latitude,
    longitude: values.longitude,
    roadAddress: values.roadAddress,
    lotAddress: values.lotAddress,
    phoneNumber: values.phoneNumber,
    thumbnailImageFileId: values.thumbnailImageFileId,
  };
}

// 가게 등록
export async function createShopAction(values: ShopFormValues): Promise<ActionResult> {
  const parsed = shopFormSchema.safeParse(values);
  if (!parsed.success) {
    return { success: false, message: parsed.error.issues[0]?.message ?? SHOP_MESSAGE.INVALID_INPUT };
  }

  const { error, data } = await shopRepository.create(toShopBody(parsed.data));
  if (error !== undefined) {
    return { success: false, message: error };
  }

  revalidatePath(SHOPS_PATH);
  return { success: true, id: data };
}

// 가게 상세 조회
export async function fetchShopAction(id: number): Promise<{ success: boolean; message?: string; data?: ShopDetail }> {
  const { error, data } = await shopService.getShop(id);
  if (error !== undefined) {
    return { success: false, message: error };
  }
  return { success: true, data };
}

// 가게 수정
export async function updateShopAction(id: number, values: ShopFormValues): Promise<ActionResult> {
  const parsed = shopFormSchema.safeParse(values);
  if (!parsed.success) {
    return { success: false, message: parsed.error.issues[0]?.message ?? SHOP_MESSAGE.INVALID_INPUT };
  }

  const { error } = await shopRepository.update(id, toShopBody(parsed.data));
  if (error !== undefined) {
    return { success: false, message: error };
  }

  revalidatePath(SHOPS_PATH);
  revalidatePath(shopDetailPath(id));
  return { success: true };
}

// 가게 폐업 처리
export async function closeShopAction(id: number): Promise<ActionResult> {
  const { error } = await shopRepository.close(id);
  if (error !== undefined) {
    return { success: false, message: error };
  }

  revalidatePath(SHOPS_PATH);
  revalidatePath(shopDetailPath(id));
  return { success: true };
}

// ===== Phase B. 운영시간 · 휴게시간 · 정기휴무일 =====

type BusinessHoursResult = { success: boolean; message?: string; data?: BusinessHour[] };
type BreakTimesResult = { success: boolean; message?: string; data?: BreakTime[] };
type ClosedDaysResult = { success: boolean; message?: string; data?: ClosedDay[] };

// 운영시간 목록 조회
export async function fetchBusinessHoursAction(shopId: number): Promise<BusinessHoursResult> {
  const { error, data } = await shopService.getBusinessHours(shopId);
  if (error !== undefined) return { success: false, message: error };
  return { success: true, data };
}

// 운영시간 등록
export async function createBusinessHourAction(shopId: number, values: BusinessHourFormValues): Promise<ActionResult> {
  const parsed = businessHourSchema.safeParse(values);
  if (!parsed.success) {
    return { success: false, message: parsed.error.issues[0]?.message ?? SHOP_MESSAGE.INVALID_INPUT };
  }
  const { error, data } = await shopRepository.createBusinessHour(shopId, parsed.data);
  if (error !== undefined) return { success: false, message: error };
  return { success: true, id: data };
}

// 운영시간 수정
export async function updateBusinessHourAction(
  businessHourId: number,
  values: BusinessHourFormValues,
): Promise<ActionResult> {
  const parsed = businessHourSchema.safeParse(values);
  if (!parsed.success) {
    return { success: false, message: parsed.error.issues[0]?.message ?? SHOP_MESSAGE.INVALID_INPUT };
  }
  const { error } = await shopRepository.updateBusinessHour(businessHourId, parsed.data);
  if (error !== undefined) return { success: false, message: error };
  return { success: true };
}

// 운영시간 삭제
export async function deleteBusinessHourAction(businessHourId: number): Promise<ActionResult> {
  const { error } = await shopRepository.deleteBusinessHour(businessHourId);
  if (error !== undefined) return { success: false, message: error };
  return { success: true };
}

// 브레이크타임 목록 조회
export async function fetchBreakTimesAction(shopId: number): Promise<BreakTimesResult> {
  const { error, data } = await shopService.getBreakTimes(shopId);
  if (error !== undefined) return { success: false, message: error };
  return { success: true, data };
}

// 브레이크타임 등록
export async function createBreakTimeAction(shopId: number, values: BreakTimeFormValues): Promise<ActionResult> {
  const parsed = breakTimeSchema.safeParse(values);
  if (!parsed.success) {
    return { success: false, message: parsed.error.issues[0]?.message ?? SHOP_MESSAGE.INVALID_INPUT };
  }
  const { error, data } = await shopRepository.createBreakTime(shopId, parsed.data);
  if (error !== undefined) return { success: false, message: error };
  return { success: true, id: data };
}

// 브레이크타임 수정
export async function updateBreakTimeAction(breakTimeId: number, values: BreakTimeFormValues): Promise<ActionResult> {
  const parsed = breakTimeSchema.safeParse(values);
  if (!parsed.success) {
    return { success: false, message: parsed.error.issues[0]?.message ?? SHOP_MESSAGE.INVALID_INPUT };
  }
  const { error } = await shopRepository.updateBreakTime(breakTimeId, parsed.data);
  if (error !== undefined) return { success: false, message: error };
  return { success: true };
}

// 브레이크타임 삭제
export async function deleteBreakTimeAction(breakTimeId: number): Promise<ActionResult> {
  const { error } = await shopRepository.deleteBreakTime(breakTimeId);
  if (error !== undefined) return { success: false, message: error };
  return { success: true };
}

// 정기휴무일 목록 조회
export async function fetchClosedDaysAction(shopId: number): Promise<ClosedDaysResult> {
  const { error, data } = await shopService.getClosedDays(shopId);
  if (error !== undefined) return { success: false, message: error };
  return { success: true, data };
}

// 정기휴무일 등록
export async function createClosedDayAction(shopId: number, values: ClosedDayFormValues): Promise<ActionResult> {
  const parsed = closedDaySchema.safeParse(values);
  if (!parsed.success) {
    return { success: false, message: parsed.error.issues[0]?.message ?? SHOP_MESSAGE.INVALID_INPUT };
  }
  const { error, data } = await shopRepository.createClosedDay(shopId, parsed.data);
  if (error !== undefined) return { success: false, message: error };
  return { success: true, id: data };
}

// 정기휴무일 삭제
export async function deleteClosedDayAction(closedDayId: number): Promise<ActionResult> {
  const { error } = await shopRepository.deleteClosedDay(closedDayId);
  if (error !== undefined) return { success: false, message: error };
  return { success: true };
}

// ===== Phase C. 편의시설 · 음식종류 · 태그 =====

type AmenityCategoriesResult = { success: boolean; message?: string; data?: AmenityCategory[] };
type FoodTypeCategoriesResult = { success: boolean; message?: string; data?: FoodTypeCategory[] };
type ShopAmenitiesResult = { success: boolean; message?: string; data?: ShopAmenity[] };
type ShopFoodTypesResult = { success: boolean; message?: string; data?: ShopFoodType[] };
type TagsResult = { success: boolean; message?: string; data?: Tag[] };

// 편의시설 마스터 카테고리 목록 조회
export async function fetchAmenityCategoriesAction(): Promise<AmenityCategoriesResult> {
  const { error, data } = await shopService.getAmenityCategories();
  if (error !== undefined) return { success: false, message: error };
  return { success: true, data };
}

// 편의시설 마스터 카테고리 등록
export async function createAmenityCategoryAction(values: AmenityCategoryFormValues): Promise<ActionResult> {
  const parsed = amenityCategorySchema.safeParse(values);
  if (!parsed.success) {
    return { success: false, message: parsed.error.issues[0]?.message ?? SHOP_MESSAGE.INVALID_INPUT };
  }
  const { error, data } = await shopRepository.createAmenityCategory(parsed.data);
  if (error !== undefined) return { success: false, message: error };
  return { success: true, id: data };
}

// 편의시설 마스터 카테고리 수정
export async function updateAmenityCategoryAction(
  categoryId: number,
  values: AmenityCategoryFormValues,
): Promise<ActionResult> {
  const parsed = amenityCategorySchema.safeParse(values);
  if (!parsed.success) {
    return { success: false, message: parsed.error.issues[0]?.message ?? SHOP_MESSAGE.INVALID_INPUT };
  }
  const { error } = await shopRepository.updateAmenityCategory(categoryId, parsed.data);
  if (error !== undefined) return { success: false, message: error };
  return { success: true };
}

// 음식종류 마스터 카테고리 목록 조회
export async function fetchFoodTypeCategoriesAction(): Promise<FoodTypeCategoriesResult> {
  const { error, data } = await shopService.getFoodTypeCategories();
  if (error !== undefined) return { success: false, message: error };
  return { success: true, data };
}

// 음식종류 마스터 카테고리 등록
export async function createFoodTypeCategoryAction(values: FoodTypeCategoryFormValues): Promise<ActionResult> {
  const parsed = foodTypeCategorySchema.safeParse(values);
  if (!parsed.success) {
    return { success: false, message: parsed.error.issues[0]?.message ?? SHOP_MESSAGE.INVALID_INPUT };
  }
  const { error, data } = await shopRepository.createFoodTypeCategory(parsed.data);
  if (error !== undefined) return { success: false, message: error };
  return { success: true, id: data };
}

// 음식종류 마스터 카테고리 수정
export async function updateFoodTypeCategoryAction(
  categoryId: number,
  values: FoodTypeCategoryFormValues,
): Promise<ActionResult> {
  const parsed = foodTypeCategorySchema.safeParse(values);
  if (!parsed.success) {
    return { success: false, message: parsed.error.issues[0]?.message ?? SHOP_MESSAGE.INVALID_INPUT };
  }
  const { error } = await shopRepository.updateFoodTypeCategory(categoryId, parsed.data);
  if (error !== undefined) return { success: false, message: error };
  return { success: true };
}

// 가게별 편의시설 지정 목록 조회
export async function fetchShopAmenitiesAction(shopId: number): Promise<ShopAmenitiesResult> {
  const { error, data } = await shopService.getShopAmenities(shopId);
  if (error !== undefined) return { success: false, message: error };
  return { success: true, data };
}

// 가게에 편의시설 지정
export async function createShopAmenityAction(shopId: number, values: ShopAmenityFormValues): Promise<ActionResult> {
  const parsed = shopAmenitySchema.safeParse(values);
  if (!parsed.success) {
    return { success: false, message: parsed.error.issues[0]?.message ?? SHOP_MESSAGE.INVALID_INPUT };
  }
  const { error } = await shopRepository.createShopAmenity(shopId, parsed.data);
  if (error !== undefined) return { success: false, message: error };
  return { success: true };
}

// 가게 편의시설 해제
export async function deleteShopAmenityAction(shopId: number, amenityCategoryId: number): Promise<ActionResult> {
  const { error } = await shopRepository.deleteShopAmenity(shopId, amenityCategoryId);
  if (error !== undefined) return { success: false, message: error };
  return { success: true };
}

// 가게별 음식종류 지정 목록 조회
export async function fetchShopFoodTypesAction(shopId: number): Promise<ShopFoodTypesResult> {
  const { error, data } = await shopService.getShopFoodTypes(shopId);
  if (error !== undefined) return { success: false, message: error };
  return { success: true, data };
}

// 가게에 음식종류 지정
export async function createShopFoodTypeAction(shopId: number, values: ShopFoodTypeFormValues): Promise<ActionResult> {
  const parsed = shopFoodTypeSchema.safeParse(values);
  if (!parsed.success) {
    return { success: false, message: parsed.error.issues[0]?.message ?? SHOP_MESSAGE.INVALID_INPUT };
  }
  const { error } = await shopRepository.createShopFoodType(shopId, parsed.data);
  if (error !== undefined) return { success: false, message: error };
  return { success: true };
}

// 가게 음식종류 해제
export async function deleteShopFoodTypeAction(shopId: number, foodTypeCategoryId: number): Promise<ActionResult> {
  const { error } = await shopRepository.deleteShopFoodType(shopId, foodTypeCategoryId);
  if (error !== undefined) return { success: false, message: error };
  return { success: true };
}

// 태그 목록 조회
export async function fetchTagsAction(): Promise<TagsResult> {
  const { error, data } = await shopService.getTags();
  if (error !== undefined) return { success: false, message: error };
  return { success: true, data };
}

// 태그 등록
export async function createTagAction(values: TagFormValues): Promise<ActionResult> {
  const parsed = tagSchema.safeParse(values);
  if (!parsed.success) {
    return { success: false, message: parsed.error.issues[0]?.message ?? SHOP_MESSAGE.INVALID_INPUT };
  }
  const { error, data } = await shopRepository.createTag(parsed.data);
  if (error !== undefined) return { success: false, message: error };
  return { success: true, id: data };
}

// 태그 삭제
export async function deleteTagAction(tagId: number): Promise<ActionResult> {
  const { error } = await shopRepository.deleteTag(tagId);
  if (error !== undefined) return { success: false, message: error };
  return { success: true };
}

// ===== Phase D. 주문수단 =====

type OrderMethodsResult = { success: boolean; message?: string; data?: OrderMethod[] };

// 가게 주문수단 목록 조회
export async function fetchOrderMethodsAction(shopId: number): Promise<OrderMethodsResult> {
  const { error, data } = await shopService.getOrderMethods(shopId);
  if (error !== undefined) return { success: false, message: error };
  return { success: true, data };
}

// 가게 주문수단 지정
export async function createOrderMethodAction(shopId: number, values: OrderMethodFormValues): Promise<ActionResult> {
  const parsed = orderMethodSchema.safeParse(values);
  if (!parsed.success) {
    return { success: false, message: parsed.error.issues[0]?.message ?? SHOP_MESSAGE.INVALID_INPUT };
  }
  const { error } = await shopRepository.createOrderMethod(shopId, parsed.data);
  if (error !== undefined) return { success: false, message: error };
  return { success: true };
}

// 가게 주문수단 해제
export async function deleteOrderMethodAction(shopId: number, orderMethod: OrderMethodValue): Promise<ActionResult> {
  const { error } = await shopRepository.deleteOrderMethod(shopId, orderMethod);
  if (error !== undefined) return { success: false, message: error };
  return { success: true };
}

// ===== Phase E. 배너 · 포토 이미지 =====

type BannersResult = { success: boolean; message?: string; data?: BannerImage[] };
type PhotoCategoriesResult = { success: boolean; message?: string; data?: PhotoCategory[] };
type PhotoImagesResult = { success: boolean; message?: string; data?: PhotoImage[] };

// 배너 이미지 목록 조회
export async function fetchBannersAction(shopId: number): Promise<BannersResult> {
  const { error, data } = await shopService.getBanners(shopId);
  if (error !== undefined) return { success: false, message: error };
  return { success: true, data };
}

// 배너 이미지 등록
export async function createBannerAction(shopId: number, values: BannerFormValues): Promise<ActionResult> {
  const parsed = bannerSchema.safeParse(values);
  if (!parsed.success) {
    return { success: false, message: parsed.error.issues[0]?.message ?? SHOP_MESSAGE.INVALID_INPUT };
  }
  const { error, data } = await shopRepository.createBanner(shopId, parsed.data);
  if (error !== undefined) return { success: false, message: error };
  return { success: true, id: data };
}

// 배너 이미지 삭제
export async function deleteBannerAction(bannerImageId: number): Promise<ActionResult> {
  const { error } = await shopRepository.deleteBanner(bannerImageId);
  if (error !== undefined) return { success: false, message: error };
  return { success: true };
}

// 포토 카테고리 목록 조회
export async function fetchPhotoCategoriesAction(shopId: number): Promise<PhotoCategoriesResult> {
  const { error, data } = await shopService.getPhotoCategories(shopId);
  if (error !== undefined) return { success: false, message: error };
  return { success: true, data };
}

// 포토 카테고리 등록
export async function createPhotoCategoryAction(
  shopId: number,
  values: PhotoCategoryFormValues,
): Promise<ActionResult> {
  const parsed = photoCategorySchema.safeParse(values);
  if (!parsed.success) {
    return { success: false, message: parsed.error.issues[0]?.message ?? SHOP_MESSAGE.INVALID_INPUT };
  }
  const { error, data } = await shopRepository.createPhotoCategory(shopId, parsed.data);
  if (error !== undefined) return { success: false, message: error };
  return { success: true, id: data };
}

// 포토 카테고리 수정
export async function updatePhotoCategoryAction(
  categoryId: number,
  values: PhotoCategoryFormValues,
): Promise<ActionResult> {
  const parsed = photoCategorySchema.safeParse(values);
  if (!parsed.success) {
    return { success: false, message: parsed.error.issues[0]?.message ?? SHOP_MESSAGE.INVALID_INPUT };
  }
  const { error } = await shopRepository.updatePhotoCategory(categoryId, parsed.data);
  if (error !== undefined) return { success: false, message: error };
  return { success: true };
}

// 포토 카테고리 삭제
export async function deletePhotoCategoryAction(categoryId: number): Promise<ActionResult> {
  const { error } = await shopRepository.deletePhotoCategory(categoryId);
  if (error !== undefined) return { success: false, message: error };
  return { success: true };
}

// 카테고리 내 이미지 목록 조회
export async function fetchPhotoCategoryImagesAction(categoryId: number): Promise<PhotoImagesResult> {
  const { error, data } = await shopService.getPhotoCategoryImages(categoryId);
  if (error !== undefined) return { success: false, message: error };
  return { success: true, data };
}

// 카테고리에 이미지 등록
export async function createPhotoCategoryImageAction(
  categoryId: number,
  values: PhotoImageFormValues,
): Promise<ActionResult> {
  const parsed = photoImageSchema.safeParse(values);
  if (!parsed.success) {
    return { success: false, message: parsed.error.issues[0]?.message ?? SHOP_MESSAGE.INVALID_INPUT };
  }
  const { error, data } = await shopRepository.createPhotoCategoryImage(categoryId, parsed.data);
  if (error !== undefined) return { success: false, message: error };
  return { success: true, id: data };
}

// 이미지 정렬/노출 수정
export async function updatePhotoCategoryImageAction(
  imageId: number,
  values: PhotoImageUpdateFormValues,
): Promise<ActionResult> {
  const parsed = photoImageUpdateSchema.safeParse(values);
  if (!parsed.success) {
    return { success: false, message: parsed.error.issues[0]?.message ?? SHOP_MESSAGE.INVALID_INPUT };
  }
  const { error } = await shopRepository.updatePhotoCategoryImage(imageId, parsed.data);
  if (error !== undefined) return { success: false, message: error };
  return { success: true };
}

// 이미지 삭제
export async function deletePhotoCategoryImageAction(imageId: number): Promise<ActionResult> {
  const { error } = await shopRepository.deletePhotoCategoryImage(imageId);
  if (error !== undefined) return { success: false, message: error };
  return { success: true };
}

// ===== Phase F. 테하 초이스 (큐레이션) =====

type EditorChoicesResult = { success: boolean; message?: string; data?: EditorChoice[] };

// 테하 초이스 목록 조회 (가게 상세에서 재조회용)
export async function fetchEditorChoicesAction(shopId: number): Promise<EditorChoicesResult> {
  const { error, data } = await shopService.getEditorChoices({ shopId }, { page: 0, size: 100 });
  if (error !== undefined) return { success: false, message: error };
  return { success: true, data };
}

// 테하 초이스 등록
export async function createEditorChoiceAction(values: EditorChoiceFormValues): Promise<ActionResult> {
  const parsed = editorChoiceSchema.safeParse(values);
  if (!parsed.success) {
    return { success: false, message: parsed.error.issues[0]?.message ?? SHOP_MESSAGE.INVALID_INPUT };
  }
  const { error, data } = await shopRepository.createEditorChoice(parsed.data);
  if (error !== undefined) return { success: false, message: error };
  return { success: true, id: data };
}

// 테하 초이스 수정
export async function updateEditorChoiceAction(
  choiceId: number,
  values: Omit<EditorChoiceFormValues, "shopId">,
): Promise<ActionResult> {
  const parsed = editorChoiceSchema.omit({ shopId: true }).safeParse(values);
  if (!parsed.success) {
    return { success: false, message: parsed.error.issues[0]?.message ?? SHOP_MESSAGE.INVALID_INPUT };
  }
  const { error } = await shopRepository.updateEditorChoice(choiceId, parsed.data);
  if (error !== undefined) return { success: false, message: error };
  return { success: true };
}

// 테하 초이스 삭제
export async function deleteEditorChoiceAction(choiceId: number): Promise<ActionResult> {
  const { error } = await shopRepository.deleteEditorChoice(choiceId);
  if (error !== undefined) return { success: false, message: error };
  return { success: true };
}
