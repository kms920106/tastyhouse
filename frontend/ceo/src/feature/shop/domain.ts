import type {
  ApprovalStatus,
  ContentBoardTopic,
  ContentBoardType,
  DayType,
  DeliveryTipSurchargeUnit,
  ExtraDeliveryTipType,
  HygieneBadgeType,
  ImageType,
  OrderMethod,
  ShopStatusValue,
  SuspensionReason,
} from "@/api/shop/shop.dto";

// api/shop 계층에서 정의한 enum 문자열 유니온을 도메인에서도 그대로 쓴다.
// UI/feature 는 DTO 인터페이스가 아닌 이 파일만 import 한다.
export type {
  ApprovalStatus,
  ContentBoardTopic,
  ContentBoardType,
  DayType,
  DeliveryTipSurchargeUnit,
  ExtraDeliveryTipType,
  HygieneBadgeType,
  ImageType,
  OrderMethod,
  ShopStatusValue,
  SuspensionReason,
};

export interface ShopSummary {
  id: number;
  name: string;
  stationName: string;
  roadAddress: string;
  permanentlyClosed: boolean;
}

export interface PhoneNumber {
  id: number;
  phoneNumber: string;
  primary: boolean;
  virtual: boolean;
}

export interface ContentBoardItem {
  id: number;
  contentType: ContentBoardType;
  topic: ContentBoardTopic;
  imageUrl: string | null;
  youtubeUrl: string | null;
  description: string;
  hidden: boolean;
}

export interface ImageChangeRequest {
  id: number;
  imageType: ImageType;
  imageUrl: string;
  status: ApprovalStatus;
  rejectReason: string | null;
}

export interface ShopImageStatus {
  currentImageUrl: string | null;
  requests: ImageChangeRequest[];
}

export interface ShopConvenienceInfo {
  parkingAvailable: boolean;
  parkingPaid: boolean;
  valetAvailable: boolean;
  valetPaid: boolean;
  directionsGuide: string;
  displayLatitude: number;
  displayLongitude: number;
}

export interface ShopAmenity {
  id: number;
  amenityCategoryId: number;
  amenity: string;
  displayName: string;
  activeFilePath: string;
}

export interface AmenityCategory {
  id: number;
  amenity: string;
  displayName: string;
  activeFilePath: string;
}

export interface ShopBasicInfo {
  id: number;
  name: string;
  latitude: number;
  longitude: number;
  roadAddress: string;
  lotAddress: string;
  phoneNumber: string;
  hidden: boolean;
  permanentlyClosed: boolean;
  closedOnPublicHolidays: boolean;
  /** 최소주문금액. 0이면 미설정(제한 없음)이며, 배달 주문에만 적용된다. */
  minOrderAmount: number;
  /** 예약주문 운영 여부. 배달·포장 주문의 수령시간 예약을 받는지 여부다. */
  scheduledOrderEnabled: boolean;
  introduction: string;
  thumbnailImageUrl: string | null;
  trademarkImageUrl: string | null;
  thumbnailStatus: ShopImageStatus;
  trademarkStatus: ShopImageStatus;
  phoneNumbers: PhoneNumber[];
  contentBoards: ContentBoardItem[];
  convenienceInfo: ShopConvenienceInfo;
  amenities: ShopAmenity[];
}

export interface BusinessHour {
  id: number;
  dayType: DayType;
  /** 서버가 내려주는 한글 라벨 — 표시에는 *_LABEL 보다 이 값을 우선한다 */
  description: string;
  openTime: string;
  closeTime: string;
  isClosed: boolean;
  is24Hours: boolean;
}

export interface BreakTime {
  id: number;
  dayType: DayType;
  description: string;
  startTime: string;
  endTime: string;
}

export interface RegularClosedDay {
  id: number;
  closedDayType: string;
  description: string;
}

export interface TemporaryClosure {
  id: number;
  startDate: string;
  endDate: string;
}

export interface ShopClosedDays {
  closedOnPublicHolidays: boolean;
  regularClosedDays: RegularClosedDay[];
  temporaryClosures: TemporaryClosure[];
}

export interface HygieneBadge {
  id: number;
  badgeType: HygieneBadgeType;
  certifiedDate: string;
  lastInspectionMonth: string;
}

export interface ShopDeliveryTipTier {
  id: number;
  tierOrder: number;
  minOrderAmount: number;
  tipAmount: number;
}

export interface ShopDeliveryTipDistance {
  baseDistanceMeters: number;
  surchargeUnit: DeliveryTipSurchargeUnit;
  surchargeAmount: number;
}

export interface ShopDeliveryTipRegion {
  id: number;
  adminDongId: number;
  /** 서버가 완성해 내려주는 행정동 전체 이름 */
  regionName: string;
  tipAmount: number;
}

export interface ShopDeliveryTipSchedule {
  id: number;
  dayType: DayType;
  startTime: string;
  endTime: string;
  tipAmount: number;
}

export interface ShopDeliveryArea {
  id: number;
  adminDongId: number;
  regionName: string;
}

export interface ShopDeliveryTipSetting {
  tiers: ShopDeliveryTipTier[];
  /** 거리별·지역별은 상호 배타이며 서버가 단일 값으로 판정해 내려준다 */
  extraTipType: ExtraDeliveryTipType;
  distance: ShopDeliveryTipDistance | null;
  regions: ShopDeliveryTipRegion[];
  schedules: ShopDeliveryTipSchedule[];
  /** 0이면 미설정 */
  holidayTipAmount: number;
}

export interface ShopOperationInfo {
  shopId: number;
  businessHours: BusinessHour[];
  breakTimes: BreakTime[];
  closedDays: ShopClosedDays;
  hygieneBadges: HygieneBadge[];
  deliveryTip: ShopDeliveryTipSetting;
  /** 지역별 배달팁의 선택 후보 — 가게에 등록된 배달가능지역 */
  deliveryAreas: ShopDeliveryArea[];
}

export interface Suspension {
  id: number;
  shopId: number;
  reason: SuspensionReason;
  /** orderMethods 를 지정하지 않고 등록하면(전체 주문유형 대상) 서버가 null 을 내려준다. */
  orderMethod: OrderMethod | null;
  startAt: string;
  endAt: string;
  releasedAt: string | null;
}
