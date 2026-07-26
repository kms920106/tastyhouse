// 가게 도메인 모델 — UI 와 api/shop.service 가 공유한다.

import type {
  Amenity,
  ClosedDayType,
  ContentBoardContentType,
  ContentBoardTopic,
  DayType,
  FoodType,
  HygieneBadgeType,
  OrderMethod as OrderMethodValue,
  ShopImageChangeStatus,
  ShopImageType,
} from "@/api/shop/shop.dto";

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
  thumbnailImageFileId: number | null;
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
  activeImageFileId: number;
  inactiveImageFileId: number;
  sort: number;
  visible: boolean;
}

export interface FoodTypeCategory {
  id: number;
  foodType: FoodType;
  displayName: string;
  activeImageFileId: number;
  inactiveImageFileId: number;
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
  imageFileId: number;
  imageUrl: string;
  sort: number;
}

export interface PhotoCategory {
  id: number;
  name: string;
}

export interface PhotoImage {
  id: number;
  imageFileId: number;
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
  imageFileId: number;
  /** 미리보기 URL. 없으면 null — 목록에서는 imageFileId 텍스트로 폴백 표시 */
  imageUrl: string | null;
  status: ShopImageChangeStatus;
  rejectReason: string | null;
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
  imageFileId: number | null;
  imageUrl: string | null;
  youtubeUrl: string | null;
  description: string;
  hidden: boolean;
  createdAt: string;
}
