import "server-only";

import { api } from "@/api/shared/client";
import type { ApiPageRequest, ApiResponse } from "@/api/shared/types";

import type {
  AmenityCategoryCreateRequest,
  AmenityCategoryResponse,
  AmenityCategoryUpdateRequest,
  BannerImageCreateRequest,
  BannerImageResponse,
  BreakTimeCreateRequest,
  BreakTimeResponse,
  BreakTimeUpdateRequest,
  BusinessHourCreateRequest,
  BusinessHourResponse,
  BusinessHourUpdateRequest,
  ClosedDayCreateRequest,
  ClosedDayResponse,
  EditorChoiceCreateRequest,
  EditorChoiceListQueryRequest,
  EditorChoiceResponse,
  EditorChoiceUpdateRequest,
  FoodTypeCategoryCreateRequest,
  FoodTypeCategoryResponse,
  FoodTypeCategoryUpdateRequest,
  OrderMethod,
  OrderMethodCreateRequest,
  OrderMethodResponse,
  PhotoCategoryCreateRequest,
  PhotoCategoryResponse,
  PhotoCategoryUpdateRequest,
  PhotoImageCreateRequest,
  PhotoImageResponse,
  PhotoImageUpdateRequest,
  ShopAmenityCreateRequest,
  ShopAmenityResponse,
  ShopCreateRequest,
  ShopDetailResponse,
  ShopFoodTypeCreateRequest,
  ShopFoodTypeResponse,
  ShopListItemResponse,
  ShopListQueryRequest,
  ShopUpdateRequest,
  StationResponse,
  TagCreateRequest,
  TagResponse,
} from "./shop.dto";

/**
 * 가게 관리자 API
 */

const ENDPOINT = "/api/shops";

export const shopRepository = {
  // ===== Phase A. 가게 본체 CRUD =====

  // 지하철역 목록 조회 (등록/수정 폼 드롭다운용)
  getStations(): Promise<ApiResponse<StationResponse[]>> {
    return api.get<StationResponse[]>(`${ENDPOINT}/v1/stations`);
  },

  // 가게 목록 조건 페이징 조회
  getList(query: ShopListQueryRequest, pageRequest: ApiPageRequest): Promise<ApiResponse<ShopListItemResponse[]>> {
    return api.get<ShopListItemResponse[]>(`${ENDPOINT}/v1`, {
      params: { ...query, ...pageRequest },
    });
  },

  // 가게 등록
  create(body: ShopCreateRequest): Promise<ApiResponse<number>> {
    return api.post<number>(`${ENDPOINT}/v1`, body);
  },

  // 가게 상세 조회
  getDetail(id: number): Promise<ApiResponse<ShopDetailResponse>> {
    return api.get<ShopDetailResponse>(`${ENDPOINT}/v1/${id}`);
  },

  // 가게 수정
  update(id: number, body: ShopUpdateRequest): Promise<ApiResponse<null>> {
    return api.put<null>(`${ENDPOINT}/v1/${id}`, body);
  },

  // 가게 폐업 처리 (body 없음)
  close(id: number): Promise<ApiResponse<null>> {
    return api.patch<null>(`${ENDPOINT}/v1/${id}/close`);
  },

  // ===== Phase B. 운영시간 · 휴게시간 · 정기휴무일 =====

  // 운영시간 목록 조회
  getBusinessHours(shopId: number): Promise<ApiResponse<BusinessHourResponse[]>> {
    return api.get<BusinessHourResponse[]>(`${ENDPOINT}/v1/${shopId}/business-hours`);
  },

  // 운영시간 등록
  createBusinessHour(shopId: number, body: BusinessHourCreateRequest): Promise<ApiResponse<number>> {
    return api.post<number>(`${ENDPOINT}/v1/${shopId}/business-hours`, body);
  },

  // 운영시간 수정 — 경로가 shopId 가 아닌 businessHourId 기준임에 주의
  updateBusinessHour(businessHourId: number, body: BusinessHourUpdateRequest): Promise<ApiResponse<null>> {
    return api.put<null>(`${ENDPOINT}/v1/business-hours/${businessHourId}`, body);
  },

  // 운영시간 삭제 — 경로가 shopId 가 아닌 businessHourId 기준임에 주의
  deleteBusinessHour(businessHourId: number): Promise<ApiResponse<null>> {
    return api.delete<null>(`${ENDPOINT}/v1/business-hours/${businessHourId}`);
  },

  // 브레이크타임 목록 조회
  getBreakTimes(shopId: number): Promise<ApiResponse<BreakTimeResponse[]>> {
    return api.get<BreakTimeResponse[]>(`${ENDPOINT}/v1/${shopId}/break-times`);
  },

  // 브레이크타임 등록
  createBreakTime(shopId: number, body: BreakTimeCreateRequest): Promise<ApiResponse<number>> {
    return api.post<number>(`${ENDPOINT}/v1/${shopId}/break-times`, body);
  },

  // 브레이크타임 수정 — 경로가 shopId 가 아닌 breakTimeId 기준임에 주의
  updateBreakTime(breakTimeId: number, body: BreakTimeUpdateRequest): Promise<ApiResponse<null>> {
    return api.put<null>(`${ENDPOINT}/v1/break-times/${breakTimeId}`, body);
  },

  // 브레이크타임 삭제 — 경로가 shopId 가 아닌 breakTimeId 기준임에 주의
  deleteBreakTime(breakTimeId: number): Promise<ApiResponse<null>> {
    return api.delete<null>(`${ENDPOINT}/v1/break-times/${breakTimeId}`);
  },

  // 정기휴무일 목록 조회
  getClosedDays(shopId: number): Promise<ApiResponse<ClosedDayResponse[]>> {
    return api.get<ClosedDayResponse[]>(`${ENDPOINT}/v1/${shopId}/closed-days`);
  },

  // 정기휴무일 등록
  createClosedDay(shopId: number, body: ClosedDayCreateRequest): Promise<ApiResponse<number>> {
    return api.post<number>(`${ENDPOINT}/v1/${shopId}/closed-days`, body);
  },

  // 정기휴무일 삭제 — 경로가 shopId 가 아닌 closedDayId 기준임에 주의
  deleteClosedDay(closedDayId: number): Promise<ApiResponse<null>> {
    return api.delete<null>(`${ENDPOINT}/v1/closed-days/${closedDayId}`);
  },

  // ===== Phase C. 편의시설 · 음식종류 · 태그 =====

  // 편의시설 마스터 카테고리 목록
  getAmenityCategories(): Promise<ApiResponse<AmenityCategoryResponse[]>> {
    return api.get<AmenityCategoryResponse[]>(`${ENDPOINT}/v1/amenity-categories`);
  },

  // 편의시설 마스터 카테고리 등록
  createAmenityCategory(body: AmenityCategoryCreateRequest): Promise<ApiResponse<number>> {
    return api.post<number>(`${ENDPOINT}/v1/amenity-categories`, body);
  },

  // 편의시설 마스터 카테고리 수정
  updateAmenityCategory(categoryId: number, body: AmenityCategoryUpdateRequest): Promise<ApiResponse<null>> {
    return api.put<null>(`${ENDPOINT}/v1/amenity-categories/${categoryId}`, body);
  },

  // 음식종류 마스터 카테고리 목록
  getFoodTypeCategories(): Promise<ApiResponse<FoodTypeCategoryResponse[]>> {
    return api.get<FoodTypeCategoryResponse[]>(`${ENDPOINT}/v1/food-type-categories`);
  },

  // 음식종류 마스터 카테고리 등록
  createFoodTypeCategory(body: FoodTypeCategoryCreateRequest): Promise<ApiResponse<number>> {
    return api.post<number>(`${ENDPOINT}/v1/food-type-categories`, body);
  },

  // 음식종류 마스터 카테고리 수정
  updateFoodTypeCategory(categoryId: number, body: FoodTypeCategoryUpdateRequest): Promise<ApiResponse<null>> {
    return api.put<null>(`${ENDPOINT}/v1/food-type-categories/${categoryId}`, body);
  },

  // 가게별 편의시설 지정 목록 조회
  getShopAmenities(shopId: number): Promise<ApiResponse<ShopAmenityResponse[]>> {
    return api.get<ShopAmenityResponse[]>(`${ENDPOINT}/v1/${shopId}/amenities`);
  },

  // 가게에 편의시설 지정
  createShopAmenity(shopId: number, body: ShopAmenityCreateRequest): Promise<ApiResponse<null>> {
    return api.post<null>(`${ENDPOINT}/v1/${shopId}/amenities`, body);
  },

  // 가게 편의시설 해제
  deleteShopAmenity(shopId: number, amenityCategoryId: number): Promise<ApiResponse<null>> {
    return api.delete<null>(`${ENDPOINT}/v1/${shopId}/amenities/${amenityCategoryId}`);
  },

  // 가게별 음식종류 지정 목록 조회
  getShopFoodTypes(shopId: number): Promise<ApiResponse<ShopFoodTypeResponse[]>> {
    return api.get<ShopFoodTypeResponse[]>(`${ENDPOINT}/v1/${shopId}/food-types`);
  },

  // 가게에 음식종류 지정
  createShopFoodType(shopId: number, body: ShopFoodTypeCreateRequest): Promise<ApiResponse<null>> {
    return api.post<null>(`${ENDPOINT}/v1/${shopId}/food-types`, body);
  },

  // 가게 음식종류 해제
  deleteShopFoodType(shopId: number, foodTypeCategoryId: number): Promise<ApiResponse<null>> {
    return api.delete<null>(`${ENDPOINT}/v1/${shopId}/food-types/${foodTypeCategoryId}`);
  },

  // 태그 목록 조회
  getTags(): Promise<ApiResponse<TagResponse[]>> {
    return api.get<TagResponse[]>(`${ENDPOINT}/v1/tags`);
  },

  // 태그 등록
  createTag(body: TagCreateRequest): Promise<ApiResponse<number>> {
    return api.post<number>(`${ENDPOINT}/v1/tags`, body);
  },

  // 태그 삭제
  deleteTag(tagId: number): Promise<ApiResponse<null>> {
    return api.delete<null>(`${ENDPOINT}/v1/tags/${tagId}`);
  },

  // ===== Phase D. 주문수단 =====

  // 가게 주문수단 목록 조회
  getOrderMethods(shopId: number): Promise<ApiResponse<OrderMethodResponse[]>> {
    return api.get<OrderMethodResponse[]>(`${ENDPOINT}/v1/${shopId}/order-methods`);
  },

  // 가게 주문수단 지정
  createOrderMethod(shopId: number, body: OrderMethodCreateRequest): Promise<ApiResponse<null>> {
    return api.post<null>(`${ENDPOINT}/v1/${shopId}/order-methods`, body);
  },

  // 가게 주문수단 해제
  deleteOrderMethod(shopId: number, orderMethod: OrderMethod): Promise<ApiResponse<null>> {
    return api.delete<null>(`${ENDPOINT}/v1/${shopId}/order-methods/${orderMethod}`);
  },

  // ===== Phase E. 배너 · 포토 이미지 =====

  // 배너 이미지 목록 조회
  getBanners(shopId: number): Promise<ApiResponse<BannerImageResponse[]>> {
    return api.get<BannerImageResponse[]>(`${ENDPOINT}/v1/${shopId}/banners`);
  },

  // 배너 이미지 등록
  createBanner(shopId: number, body: BannerImageCreateRequest): Promise<ApiResponse<number>> {
    return api.post<number>(`${ENDPOINT}/v1/${shopId}/banners`, body);
  },

  // 배너 이미지 삭제
  deleteBanner(bannerImageId: number): Promise<ApiResponse<null>> {
    return api.delete<null>(`${ENDPOINT}/v1/banners/${bannerImageId}`);
  },

  // 포토 카테고리 목록 조회
  getPhotoCategories(shopId: number): Promise<ApiResponse<PhotoCategoryResponse[]>> {
    return api.get<PhotoCategoryResponse[]>(`${ENDPOINT}/v1/${shopId}/photo-categories`);
  },

  // 포토 카테고리 등록
  createPhotoCategory(shopId: number, body: PhotoCategoryCreateRequest): Promise<ApiResponse<number>> {
    return api.post<number>(`${ENDPOINT}/v1/${shopId}/photo-categories`, body);
  },

  // 포토 카테고리 수정
  updatePhotoCategory(categoryId: number, body: PhotoCategoryUpdateRequest): Promise<ApiResponse<null>> {
    return api.put<null>(`${ENDPOINT}/v1/photo-categories/${categoryId}`, body);
  },

  // 포토 카테고리 삭제
  deletePhotoCategory(categoryId: number): Promise<ApiResponse<null>> {
    return api.delete<null>(`${ENDPOINT}/v1/photo-categories/${categoryId}`);
  },

  // 카테고리 내 이미지 목록 조회
  getPhotoCategoryImages(categoryId: number): Promise<ApiResponse<PhotoImageResponse[]>> {
    return api.get<PhotoImageResponse[]>(`${ENDPOINT}/v1/photo-categories/${categoryId}/images`);
  },

  // 카테고리에 이미지 등록
  createPhotoCategoryImage(categoryId: number, body: PhotoImageCreateRequest): Promise<ApiResponse<number>> {
    return api.post<number>(`${ENDPOINT}/v1/photo-categories/${categoryId}/images`, body);
  },

  // 이미지 정렬/노출 수정
  updatePhotoCategoryImage(imageId: number, body: PhotoImageUpdateRequest): Promise<ApiResponse<null>> {
    return api.put<null>(`${ENDPOINT}/v1/photo-categories/images/${imageId}`, body);
  },

  // 이미지 삭제
  deletePhotoCategoryImage(imageId: number): Promise<ApiResponse<null>> {
    return api.delete<null>(`${ENDPOINT}/v1/photo-categories/images/${imageId}`);
  },

  // ===== Phase F. 테하 초이스 (큐레이션) =====

  // 테하 초이스 목록 조회 (페이징)
  getEditorChoices(
    query: EditorChoiceListQueryRequest,
    pageRequest: ApiPageRequest,
  ): Promise<ApiResponse<EditorChoiceResponse[]>> {
    return api.get<EditorChoiceResponse[]>(`${ENDPOINT}/v1/editor-choices`, {
      params: { ...query, ...pageRequest },
    });
  },

  // 테하 초이스 등록
  createEditorChoice(body: EditorChoiceCreateRequest): Promise<ApiResponse<number>> {
    return api.post<number>(`${ENDPOINT}/v1/editor-choices`, body);
  },

  // 테하 초이스 수정
  updateEditorChoice(choiceId: number, body: EditorChoiceUpdateRequest): Promise<ApiResponse<null>> {
    return api.put<null>(`${ENDPOINT}/v1/editor-choices/${choiceId}`, body);
  },

  // 테하 초이스 삭제
  deleteEditorChoice(choiceId: number): Promise<ApiResponse<null>> {
    return api.delete<null>(`${ENDPOINT}/v1/editor-choices/${choiceId}`);
  },
};
