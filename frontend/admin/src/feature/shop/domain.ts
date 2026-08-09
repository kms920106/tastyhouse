// 가게 도메인 모델 — UI 와 api/shop.service 가 공유한다.

import type {
  Amenity,
  ClosedDayType,
  ContentBoardContentType,
  ContentBoardTopic,
  DayType,
  DeliveryAreaAdjustmentStatus,
  FoodType,
  HygieneBadgeType,
  OrderMethod as OrderMethodValue,
  RiderGuideActionType,
  RiderGuideActorType,
  ShopImageChangeStatus,
  ShopImageType,
} from "@/api/shop/shop.dto";

// api/shop 계층에서 정의한 enum 문자열 유니온을 도메인에서도 그대로 쓴다.
export type { DeliveryAreaAdjustmentStatus, RiderGuideActionType, RiderGuideActorType };

export interface Station {
  id: number;
  stationName: string;
}

export interface Ceo {
  id: number;
  name: string;
  businessRegistrationNumber: string;
  status: "ACTIVE" | "INACTIVE";
}

export interface ShopListItem {
  id: number;
  name: string;
  stationName: string;
  roadAddress: string;
  rating: number | null;
  permanentlyClosed: boolean;
}

export interface ShopDetail {
  id: number;
  stationId: number;
  name: string;
  latitude: number;
  longitude: number;
  rating: number | null;
  roadAddress: string;
  lotAddress: string;
  phoneNumber: string | null;
  thumbnailImageUrl: string | null;
  permanentlyClosed: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface BusinessHour {
  id: number;
  dayType: DayType;
  /** 서버가 내려주는 한글 라벨 (표시용) */
  description: string;
  openTime: string;
  closeTime: string;
  isClosed: boolean;
  is24Hours: boolean;
}

export interface BreakTime {
  id: number;
  dayType: DayType;
  /** 서버가 내려주는 한글 라벨 (표시용) */
  description: string;
  startTime: string;
  endTime: string;
}

export interface ClosedDay {
  id: number;
  closedDayType: ClosedDayType;
  /** 서버가 내려주는 한글 라벨 (표시용) */
  description: string;
}

export interface AmenityCategory {
  id: number;
  amenity: Amenity;
  displayName: string;
  activeImageUrl: string;
  inactiveImageUrl: string;
  sort: number;
  visible: boolean;
}

export interface FoodTypeCategory {
  id: number;
  foodType: FoodType;
  displayName: string;
  activeImageUrl: string;
  inactiveImageUrl: string;
  sort: number;
  visible: boolean;
}

export interface ShopAmenity {
  id: number;
  amenityCategoryId: number;
  amenity: Amenity;
  displayName: string;
  activeFilePath: string;
}

export interface ShopFoodType {
  id: number;
  foodTypeCategoryId: number;
  foodType: FoodType;
  displayName: string;
  activeFilePath: string;
}

export interface Tag {
  id: number;
  tagName: string;
}

export interface OrderMethod {
  orderMethod: OrderMethodValue;
  /** 서버가 내려주는 한글 라벨 (표시용) */
  displayName: string;
}

export interface BannerImage {
  id: number;
  imageUrl: string;
  sort: number;
}

export interface PhotoCategory {
  id: number;
  name: string;
}

export interface PhotoImage {
  id: number;
  imageUrl: string;
  sort: number;
  visible: boolean;
}

export interface EditorChoice {
  id: number;
  shopId: number;
  shopName: string;
  title: string;
  content: string;
  createdAt: string;
  updatedAt: string;
}

export interface ShopImageChangeRequest {
  id: number;
  shopId: number;
  imageType: ShopImageType;
  /** 미리보기 URL. 없으면 null */
  imageUrl: string | null;
  status: ShopImageChangeStatus;
  rejectReason: string | null;
}

/**
 * 배달지역 조정 신청 목록 한 건.
 *
 * 조정 완료(COMPLETED)는 조정 성립 사실의 기록일 뿐이며 배달가능지역이 자동 반영되지는 않는다.
 * 실제 반영은 배달가능지역 등록/삭제로 별도 수행한다.
 */
export interface DeliveryAreaAdjustmentListItem {
  id: number;
  shopId: number;
  shopName: string;
  counterpartShopName: string;
  franchiseName: string;
  status: DeliveryAreaAdjustmentStatus;
  createdAt: string;
}

export interface DeliveryAreaAdjustmentDetail extends DeliveryAreaAdjustmentListItem {
  counterpartBusinessNumber: string;
  reason: string;
  /** 동의서 표시용 URL. 없으면 null */
  consentFileUrl: string | null;
  /** 반려 사유. 반려가 아니면 null */
  rejectReason: string | null;
  updatedAt: string;
}

export interface ShopHygieneBadge {
  id: number;
  shopId: number;
  badgeType: HygieneBadgeType;
  /** LocalDate (YYYY-MM-DD) */
  certifiedDate: string;
  /** 세스코 최근 점검월 (YYYY-MM), 없으면 null */
  lastInspectionMonth: string | null;
}

export interface ContentBoard {
  id: number;
  shopId: number;
  contentType: ContentBoardContentType;
  topic: ContentBoardTopic;
  imageUrl: string | null;
  youtubeUrl: string | null;
  description: string;
  hidden: boolean;
  createdAt: string;
}

export interface ShopRiderGuideListItem {
  shopId: number;
  shopName: string;
  /** 미등록 시 null */
  visitGuide: string | null;
  hasPickupLocation: boolean;
  updatedAt: string;
}

export interface ShopRiderPickupLocation {
  roadAddress: string;
  lotAddress: string | null;
  detailAddress: string | null;
  latitude: number;
  longitude: number;
}

export interface ShopRiderGuideHistory {
  id: number;
  actorType: RiderGuideActorType;
  actorId: number;
  actionType: RiderGuideActionType;
  previousVisitGuide: string | null;
  /** 삭제 조치 시 null */
  newVisitGuide: string | null;
  /** 점주 변경(UPDATE) 시 null */
  reason: string | null;
  createdAt: string;
}

export interface ShopRiderGuideDetail {
  shopId: number;
  shopName: string;
  shopRoadAddress: string;
  visitGuide: string | null;
  /** 미설정 시 null — 라이더에게는 가게 실주소가 폴백으로 안내된다 */
  pickupLocation: ShopRiderPickupLocation | null;
  /** 최신순, 최대 20건 */
  histories: ShopRiderGuideHistory[];
}
