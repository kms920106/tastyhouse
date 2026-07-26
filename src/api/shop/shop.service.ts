import "server-only";

import type { ApiPageRequest, ApiResponse } from "@/api/shared/types";
import type {
  BusinessHour,
  ContentBoardItem,
  PhoneNumber,
  ShopAmenity,
  ShopBasicInfo,
  ShopImageStatus,
  ShopOperationInfo,
  ShopSummary,
} from "@/feature/shop/domain";

import type {
  BusinessHourResponse,
  ImageChangeRequestResponse,
  ShopImageStatusResponse,
  ShopListQueryRequest,
} from "./shop.dto";
import { shopRepository } from "./shop.repository";

function toImageStatus(res: ShopImageStatusResponse | undefined): ShopImageStatus {
  return {
    currentImageFileId: res?.currentImageFileId ?? null,
    requests: (res?.requests ?? []).map(toImageChangeRequest),
  };
}

function toImageChangeRequest(item: ImageChangeRequestResponse) {
  return {
    id: item.id,
    imageType: item.imageType,
    imageFileId: item.imageFileId,
    status: item.status,
    rejectReason: item.rejectReason,
  };
}

function toBusinessHour(item: BusinessHourResponse): BusinessHour {
  return {
    id: item.id,
    dayType: item.dayType,
    description: item.description,
    openTime: item.openTime,
    closeTime: item.closeTime,
    isClosed: item.isClosed,
    is24Hours: item.is24Hours,
  };
}

export const shopService = {
  // 내 가게 목록 조회 — 도메인 반환
  async getMyShops(query: ShopListQueryRequest, pageRequest: ApiPageRequest): Promise<ApiResponse<ShopSummary[]>> {
    const res = await shopRepository.getList(query, pageRequest);
    return {
      ...res,
      data: res.data?.map((item) => ({
        id: item.id,
        name: item.name,
        stationName: item.stationName,
        roadAddress: item.roadAddress,
        permanentlyClosed: item.permanentlyClosed,
      })),
    };
  },

  // 기본정보 탭 1회 렌더에 필요한 데이터를 병합 조회 — 하나라도 실패하면 그 error 를 그대로 올린다
  async getShopBasicInfo(shopId: number): Promise<ApiResponse<ShopBasicInfo>> {
    const [
      detailRes,
      introductionRes,
      phoneRes,
      contentBoardRes,
      convenienceInfoRes,
      amenitiesRes,
      thumbnailRes,
      trademarkRes,
    ] = await Promise.all([
      shopRepository.getDetail(shopId),
      shopRepository.getIntroduction(shopId),
      shopRepository.getPhoneNumbers(shopId),
      shopRepository.getContentBoards(shopId),
      shopRepository.getConvenienceInfo(shopId),
      shopRepository.getAmenities(shopId),
      shopRepository.getThumbnail(shopId),
      shopRepository.getTrademark(shopId),
    ]);

    const failed = [
      detailRes,
      introductionRes,
      phoneRes,
      contentBoardRes,
      convenienceInfoRes,
      amenitiesRes,
      thumbnailRes,
      trademarkRes,
    ].find((res) => res.error !== undefined);
    if (failed) return { error: failed.error, status: failed.status };

    const detail = detailRes.data;
    if (!detail) return { status: detailRes.status };

    const phoneNumbers: PhoneNumber[] = (phoneRes.data ?? []).map((item) => ({
      id: item.id,
      phoneNumber: item.phoneNumber,
      primary: item.primary,
      virtual: item.virtual,
    }));

    const contentBoards: ContentBoardItem[] = (contentBoardRes.data ?? []).map((item) => ({
      id: item.id,
      contentType: item.contentType,
      topic: item.topic,
      imageFileId: item.imageFileId,
      youtubeUrl: item.youtubeUrl,
      description: item.description,
      hidden: item.hidden,
    }));

    const amenities: ShopAmenity[] = (amenitiesRes.data ?? []).map((item) => ({
      id: item.id,
      amenityCategoryId: item.amenityCategoryId,
      amenity: item.amenity,
      displayName: item.displayName,
      activeFilePath: item.activeFilePath,
    }));

    const convenienceInfo = convenienceInfoRes.data;

    return {
      status: detailRes.status,
      data: {
        id: detail.id,
        name: detail.name,
        latitude: detail.latitude,
        longitude: detail.longitude,
        roadAddress: detail.roadAddress,
        lotAddress: detail.lotAddress,
        phoneNumber: detail.phoneNumber,
        hidden: detail.hidden,
        permanentlyClosed: detail.permanentlyClosed,
        closedOnPublicHolidays: detail.closedOnPublicHolidays,
        introduction: introductionRes.data?.message ?? "",
        thumbnailImageFileId: detail.thumbnailImageFileId,
        trademarkImageFileId: detail.trademarkImageFileId,
        thumbnailStatus: toImageStatus(thumbnailRes.data),
        trademarkStatus: toImageStatus(trademarkRes.data),
        phoneNumbers,
        contentBoards,
        convenienceInfo: {
          parkingAvailable: convenienceInfo?.parkingAvailable ?? false,
          parkingPaid: convenienceInfo?.parkingPaid ?? false,
          valetAvailable: convenienceInfo?.valetAvailable ?? false,
          valetPaid: convenienceInfo?.valetPaid ?? false,
          directionsGuide: convenienceInfo?.directionsGuide ?? "",
          displayLatitude: convenienceInfo?.displayLatitude ?? detail.latitude,
          displayLongitude: convenienceInfo?.displayLongitude ?? detail.longitude,
        },
        amenities,
      },
    };
  },

  // 운영정보 탭 1회 렌더에 필요한 데이터를 병합 조회 — 하나라도 실패하면 그 error 를 그대로 올린다
  async getShopOperationInfo(shopId: number): Promise<ApiResponse<ShopOperationInfo>> {
    const [businessHourRes, breakTimeRes, closedDaysRes, hygieneRes] = await Promise.all([
      shopRepository.getBusinessHours(shopId),
      shopRepository.getBreakTimes(shopId),
      shopRepository.getClosedDays(shopId),
      shopRepository.getHygieneBadges(shopId),
    ]);

    const failed = [businessHourRes, breakTimeRes, closedDaysRes, hygieneRes].find((res) => res.error !== undefined);
    if (failed) return { error: failed.error, status: failed.status };

    const closedDays = closedDaysRes.data;

    return {
      status: businessHourRes.status,
      data: {
        shopId,
        businessHours: (businessHourRes.data ?? []).map(toBusinessHour),
        breakTimes: (breakTimeRes.data ?? []).map((item) => ({
          id: item.id,
          dayType: item.dayType,
          description: item.description,
          startTime: item.startTime,
          endTime: item.endTime,
        })),
        closedDays: {
          closedOnPublicHolidays: closedDays?.closedOnPublicHolidays ?? false,
          regularClosedDays: (closedDays?.regularClosedDays ?? []).map((item) => ({
            id: item.id,
            closedDayType: item.closedDayType,
            description: item.description,
          })),
          temporaryClosures: (closedDays?.temporaryClosures ?? []).map((item) => ({
            id: item.id,
            startDate: item.startDate,
            endDate: item.endDate,
          })),
        },
        hygieneBadges: (hygieneRes.data ?? []).map((item) => ({
          id: item.id,
          badgeType: item.badgeType,
          certifiedDate: item.certifiedDate,
          lastInspectionMonth: item.lastInspectionMonth,
        })),
      },
    };
  },
};
