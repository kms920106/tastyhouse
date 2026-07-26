import type {
  ApprovalStatus,
  ContentBoardTopic,
  ContentBoardType,
  DayType,
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
  imageFileId: number | null;
  youtubeUrl: string | null;
  description: string;
  hidden: boolean;
}

export interface ImageChangeRequest {
  id: number;
  imageType: ImageType;
  imageFileId: number;
  status: ApprovalStatus;
  rejectReason: string | null;
}

export interface ShopImageStatus {
  currentImageFileId: number | null;
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
  introduction: string;
  thumbnailImageFileId: number | null;
  trademarkImageFileId: number | null;
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

export interface ShopOperationInfo {
  shopId: number;
  businessHours: BusinessHour[];
  breakTimes: BreakTime[];
  closedDays: ShopClosedDays;
  hygieneBadges: HygieneBadge[];
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
