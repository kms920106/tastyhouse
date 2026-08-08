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
  ShopOrderAvailability,
  ShopRiderGuide,
  ShopSummary,
} from "@/feature/shop/domain";
import logger from "@/lib/logger";

import type {
  BusinessHourResponse,
  ImageChangeRequestResponse,
  ShopImageStatusResponse,
  ShopListQueryRequest,
  ShopRiderGuideResponse,
} from "./shop.dto";
import { shopRepository } from "./shop.repository";

function toImageStatus(res: ShopImageStatusResponse | undefined): ShopImageStatus {
  return {
    currentImageUrl: res?.currentImageUrl ?? null,
    requests: (res?.requests ?? []).map(toImageChangeRequest),
  };
}

function toImageChangeRequest(item: ImageChangeRequestResponse) {
  return {
    id: item.id,
    imageType: item.imageType,
    imageUrl: item.imageUrl,
    status: item.status,
    rejectReason: item.rejectReason,
  };
}

// 라이더 안내 조회는 실패해도 탭을 막지 않으므로, undefined 를 '미설정' 기본값으로 되돌린다.
// 가게 실주소·좌표 폴백값도 함께 비어 있게 되지만, 그 상태에서는 시트를 열어도 저장 전 서버가 다시 판정한다.
function toRiderGuide(res: ShopRiderGuideResponse | undefined): ShopRiderGuide {
  return {
    visitGuide: res?.visitGuide ?? null,
    pickupLocation: res?.pickupLocation
      ? {
          roadAddress: res.pickupLocation.roadAddress,
          lotAddress: res.pickupLocation.lotAddress,
          detailAddress: res.pickupLocation.detailAddress,
          latitude: res.pickupLocation.latitude,
          longitude: res.pickupLocation.longitude,
        }
      : null,
    shopRoadAddress: res?.shopRoadAddress ?? "",
    shopLotAddress: res?.shopLotAddress ?? null,
    shopLatitude: res?.shopLatitude ?? 0,
    shopLongitude: res?.shopLongitude ?? 0,
    updatedAt: res?.updatedAt ?? null,
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
      imageUrl: item.imageUrl,
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
        minOrderAmount: detail.minOrderAmount,
        scheduledOrderEnabled: detail.scheduledOrderEnabled,
        introduction: introductionRes.data?.message ?? "",
        thumbnailImageUrl: detail.thumbnailImageUrl,
        trademarkImageUrl: detail.trademarkImageUrl,
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

  // 운영정보 탭 1회 렌더에 필요한 데이터를 병합 조회 — 하나라도 실패하면 그 error 를 그대로 올린다.
  // 배달팁·배달가능지역도 운영정보 탭에서 함께 노출하므로 같은 병합 조회에 포함한다.
  async getShopOperationInfo(shopId: number): Promise<ApiResponse<ShopOperationInfo>> {
    const [businessHourRes, breakTimeRes, closedDaysRes, hygieneRes, deliveryTipRes, deliveryAreaRes, riderGuideRes] =
      await Promise.all([
        shopRepository.getBusinessHours(shopId),
        shopRepository.getBreakTimes(shopId),
        shopRepository.getClosedDays(shopId),
        shopRepository.getHygieneBadges(shopId),
        shopRepository.getDeliveryTips(shopId),
        shopRepository.getDeliveryAreas(shopId),
        shopRepository.getRiderGuide(shopId),
      ]);

    const failed = [businessHourRes, breakTimeRes, closedDaysRes, hygieneRes].find((res) => res.error !== undefined);
    if (failed) return { error: failed.error, status: failed.status };

    // 배달팁·배달가능지역 조회 실패는 탭 전체를 막지 않는다.
    // 두 항목은 운영정보 탭의 부가 설정이므로, 실패 시 '미설정' 상태로 렌더해 영업시간·휴무일 편집을 계속 쓸 수 있게 한다.
    if (deliveryTipRes.error !== undefined) {
      logger.warn({ reason: deliveryTipRes.error, shopId }, "가게 배달팁 조회 실패 — 미설정으로 렌더");
    }
    if (deliveryAreaRes.error !== undefined) {
      logger.warn({ reason: deliveryAreaRes.error, shopId }, "가게 배달가능지역 조회 실패 — 빈 목록으로 렌더");
    }
    // 라이더 안내도 같은 이유로 탭 전체를 막지 않는다 — 부가 설정이므로 '미설정' 상태로 렌더한다.
    if (riderGuideRes.error !== undefined) {
      logger.warn({ reason: riderGuideRes.error, shopId }, "가게 라이더 안내 조회 실패 — 미설정으로 렌더");
    }

    const closedDays = closedDaysRes.data;
    const deliveryTip = deliveryTipRes.data;

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
        deliveryTip: {
          tiers: (deliveryTip?.tiers ?? []).map((item) => ({
            id: item.id,
            tierOrder: item.tierOrder,
            minOrderAmount: item.minOrderAmount,
            tipAmount: item.tipAmount,
          })),
          extraTipType: deliveryTip?.extraTipType ?? "NONE",
          distance: deliveryTip?.distance
            ? {
                baseDistanceMeters: deliveryTip.distance.baseDistanceMeters,
                surchargeUnit: deliveryTip.distance.surchargeUnit,
                surchargeAmount: deliveryTip.distance.surchargeAmount,
              }
            : null,
          regions: (deliveryTip?.regions ?? []).map((item) => ({
            id: item.id,
            adminDongId: item.adminDongId,
            regionName: item.regionName,
            tipAmount: item.tipAmount,
          })),
          schedules: (deliveryTip?.schedules ?? []).map((item) => ({
            id: item.id,
            dayType: item.dayType,
            startTime: item.startTime,
            endTime: item.endTime,
            tipAmount: item.tipAmount,
          })),
          holidayTipAmount: deliveryTip?.holidayTipAmount ?? 0,
        },
        deliveryAreas: (deliveryAreaRes.data ?? []).map((item) => ({
          id: item.id,
          adminDongId: item.adminDongId,
          regionName: item.regionName,
        })),
        riderGuide: toRiderGuide(riderGuideRes.data),
      },
    };
  },

  // 주문정보 탭 — 가게 주문가능 상태와 주문유형별 상태를 조회한다.
  // 실시간 값이라 캐시하지 않으며, 표시 문구(orderMethodName·unavailableReasonName)는 서버 값을 그대로 옮긴다.
  async getShopOrderAvailability(shopId: number): Promise<ApiResponse<ShopOrderAvailability>> {
    const res = await shopRepository.getOrderAvailability(shopId);
    if (res.error !== undefined || !res.data) return { error: res.error, status: res.status };

    const { orderable, unavailableReason, unavailableReasonName, orderMethods } = res.data;

    return {
      status: res.status,
      data: {
        orderable,
        unavailableReason,
        unavailableReasonName,
        orderMethods: orderMethods.map((item) => ({
          orderMethod: item.orderMethod,
          orderMethodName: item.orderMethodName,
          orderable: item.orderable,
          unavailableReason: item.unavailableReason,
          unavailableReasonName: item.unavailableReasonName,
        })),
      },
    };
  },
};
