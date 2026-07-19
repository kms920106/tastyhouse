import "server-only";

import type { ApiPageRequest, ApiResponse } from "@/api/shared/types";
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
  ShopListItem,
  Station,
  Tag,
} from "@/feature/shop/domain";

import type { EditorChoiceListQueryRequest, ShopListQueryRequest } from "./shop.dto";
import { shopRepository } from "./shop.repository";

export const shopService = {
  // 지하철역 목록 조회 — 도메인 반환
  async getStations(): Promise<ApiResponse<Station[]>> {
    const res = await shopRepository.getStations();
    return {
      ...res,
      data: res.data?.map((item) => ({ id: item.id, stationName: item.stationName })),
    };
  },

  // 가게 목록 조회 — 도메인 반환
  async getShops(query: ShopListQueryRequest, pageRequest: ApiPageRequest): Promise<ApiResponse<ShopListItem[]>> {
    const res = await shopRepository.getList(query, pageRequest);
    return {
      ...res,
      data: res.data?.map((item) => ({
        id: item.id,
        name: item.name,
        stationName: item.stationName,
        roadAddress: item.roadAddress,
        rating: item.rating,
        permanentlyClosed: item.permanentlyClosed,
      })),
    };
  },

  // 가게 상세 조회 — 도메인 반환
  async getShop(id: number): Promise<ApiResponse<ShopDetail>> {
    const res = await shopRepository.getDetail(id);
    if (!res.data) return { ...res, data: undefined };
    return {
      ...res,
      data: {
        id: res.data.id,
        stationId: res.data.stationId,
        name: res.data.name,
        latitude: res.data.latitude,
        longitude: res.data.longitude,
        rating: res.data.rating,
        roadAddress: res.data.roadAddress,
        lotAddress: res.data.lotAddress,
        phoneNumber: res.data.phoneNumber,
        thumbnailImageFileId: res.data.thumbnailImageFileId,
        permanentlyClosed: res.data.permanentlyClosed,
        createdAt: res.data.createdAt,
        updatedAt: res.data.updatedAt,
      },
    };
  },

  // 운영시간 목록 조회 — 도메인 반환
  async getBusinessHours(shopId: number): Promise<ApiResponse<BusinessHour[]>> {
    const res = await shopRepository.getBusinessHours(shopId);
    return {
      ...res,
      data: res.data?.map((item) => ({
        id: item.id,
        dayType: item.dayType,
        description: item.description,
        openTime: item.openTime,
        closeTime: item.closeTime,
        isClosed: item.isClosed,
      })),
    };
  },

  // 브레이크타임 목록 조회 — 도메인 반환
  async getBreakTimes(shopId: number): Promise<ApiResponse<BreakTime[]>> {
    const res = await shopRepository.getBreakTimes(shopId);
    return {
      ...res,
      data: res.data?.map((item) => ({
        id: item.id,
        dayType: item.dayType,
        description: item.description,
        startTime: item.startTime,
        endTime: item.endTime,
      })),
    };
  },

  // 정기휴무일 목록 조회 — 도메인 반환
  async getClosedDays(shopId: number): Promise<ApiResponse<ClosedDay[]>> {
    const res = await shopRepository.getClosedDays(shopId);
    return {
      ...res,
      data: res.data?.map((item) => ({
        id: item.id,
        closedDayType: item.closedDayType,
        description: item.description,
      })),
    };
  },

  // 편의시설 마스터 카테고리 목록 조회 — 도메인 반환
  async getAmenityCategories(): Promise<ApiResponse<AmenityCategory[]>> {
    const res = await shopRepository.getAmenityCategories();
    return {
      ...res,
      data: res.data?.map((item) => ({
        id: item.id,
        amenity: item.amenity,
        displayName: item.displayName,
        activeImageFileId: item.activeImageFileId,
        inactiveImageFileId: item.inactiveImageFileId,
        sort: item.sort,
        visible: item.visible,
      })),
    };
  },

  // 음식종류 마스터 카테고리 목록 조회 — 도메인 반환
  async getFoodTypeCategories(): Promise<ApiResponse<FoodTypeCategory[]>> {
    const res = await shopRepository.getFoodTypeCategories();
    return {
      ...res,
      data: res.data?.map((item) => ({
        id: item.id,
        foodType: item.foodType,
        displayName: item.displayName,
        activeImageFileId: item.activeImageFileId,
        inactiveImageFileId: item.inactiveImageFileId,
        sort: item.sort,
        visible: item.visible,
      })),
    };
  },

  // 가게별 편의시설 지정 목록 조회 — 도메인 반환
  async getShopAmenities(shopId: number): Promise<ApiResponse<ShopAmenity[]>> {
    const res = await shopRepository.getShopAmenities(shopId);
    return {
      ...res,
      data: res.data?.map((item) => ({
        id: item.id,
        amenityCategoryId: item.amenityCategoryId,
        amenity: item.amenity,
        displayName: item.displayName,
        activeFilePath: item.activeFilePath,
      })),
    };
  },

  // 가게별 음식종류 지정 목록 조회 — 도메인 반환
  async getShopFoodTypes(shopId: number): Promise<ApiResponse<ShopFoodType[]>> {
    const res = await shopRepository.getShopFoodTypes(shopId);
    return {
      ...res,
      data: res.data?.map((item) => ({
        id: item.id,
        foodTypeCategoryId: item.foodTypeCategoryId,
        foodType: item.foodType,
        displayName: item.displayName,
        activeFilePath: item.activeFilePath,
      })),
    };
  },

  // 태그 목록 조회 — 도메인 반환
  async getTags(): Promise<ApiResponse<Tag[]>> {
    const res = await shopRepository.getTags();
    return {
      ...res,
      data: res.data?.map((item) => ({ id: item.id, tagName: item.tagName })),
    };
  },

  // 가게 주문수단 목록 조회 — 도메인 반환
  async getOrderMethods(shopId: number): Promise<ApiResponse<OrderMethod[]>> {
    const res = await shopRepository.getOrderMethods(shopId);
    return {
      ...res,
      data: res.data?.map((item) => ({ orderMethod: item.orderMethod, displayName: item.displayName })),
    };
  },

  // 배너 이미지 목록 조회 — 도메인 반환
  async getBanners(shopId: number): Promise<ApiResponse<BannerImage[]>> {
    const res = await shopRepository.getBanners(shopId);
    return {
      ...res,
      data: res.data?.map((item) => ({
        id: item.id,
        imageFileId: item.imageFileId,
        imageUrl: item.imageUrl,
        sort: item.sort,
      })),
    };
  },

  // 포토 카테고리 목록 조회 — 도메인 반환
  async getPhotoCategories(shopId: number): Promise<ApiResponse<PhotoCategory[]>> {
    const res = await shopRepository.getPhotoCategories(shopId);
    return {
      ...res,
      data: res.data?.map((item) => ({ id: item.id, name: item.name })),
    };
  },

  // 카테고리 내 이미지 목록 조회 — 도메인 반환
  async getPhotoCategoryImages(categoryId: number): Promise<ApiResponse<PhotoImage[]>> {
    const res = await shopRepository.getPhotoCategoryImages(categoryId);
    return {
      ...res,
      data: res.data?.map((item) => ({
        id: item.id,
        imageFileId: item.imageFileId,
        imageUrl: item.imageUrl,
        sort: item.sort,
        visible: item.visible,
      })),
    };
  },

  // 테하 초이스 목록 조회 — 도메인 반환
  async getEditorChoices(
    query: EditorChoiceListQueryRequest,
    pageRequest: ApiPageRequest,
  ): Promise<ApiResponse<EditorChoice[]>> {
    const res = await shopRepository.getEditorChoices(query, pageRequest);
    return {
      ...res,
      data: res.data?.map((item) => ({
        id: item.id,
        shopId: item.shopId,
        shopName: item.shopName,
        title: item.title,
        content: item.content,
        createdAt: item.createdAt,
        updatedAt: item.updatedAt,
      })),
    };
  },
};
