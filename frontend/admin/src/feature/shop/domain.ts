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
  MenuCollectionImageStatus,
  OrderMethod as OrderMethodValue,
  RiderGuideActionType,
  RiderGuideActorType,
  ShopImageChangeStatus,
  ShopImageType,
  StorePriceVerificationStatus,
} from "@/api/shop/shop.dto";

// api/shop 계층에서 정의한 enum 문자열 유니온을 도메인에서도 그대로 쓴다.
export type {
  DeliveryAreaAdjustmentStatus,
  MenuCollectionImageStatus,
  RiderGuideActionType,
  RiderGuideActorType,
  StorePriceVerificationStatus,
};

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
  /** 일회용컵 보증금제 대상사업자 여부 — admin 만 토글한다(외부 규제 사실이라 점주는 스스로 켤 수 없다) */
  cupDepositEnabled: boolean;
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

export interface ShopOrderNotice {
  /** 미설정이면 null */
  content: string | null;
  hidden: boolean;
  /** 게시중이면 null */
  hiddenReason: string | null;
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

/**
 * 메뉴모음컷 승인요청 한 건.
 *
 * `ShopImageChangeRequest`(상표·대표이미지)와 달리 점주 취소분(CANCELED)이 있어 상태 유니온이 다르다.
 */
export interface MenuCollectionImageRequestItem {
  id: number;
  shopId: number;
  shopName: string;
  /** 검수 대상 이미지 URL. 없으면 null — 이미지를 못 보면 승인 판단이 불가하다 */
  imageUrl: string | null;
  sort: number;
  status: MenuCollectionImageStatus;
  rejectReason: string | null;
}

// ===== 매장가격 인증 검수 =====

/**
 * 매장가격 인증 요청 대상 메뉴 한 건.
 *
 * <p>검수 기준("매장보다 앱 가격이 높은 메뉴는 반려")을 판단하려면 현재 앱 배달가와
 * 이번에 요청한 매장가를 같은 화면에서 대조할 수 있어야 하므로 두 값을 함께 보관한다.
 */
export interface StorePriceVerificationRequestTargetItem {
  productId: number;
  productName: string;
  /** 가격명(보통/곱빼기 등). 단일 가격 메뉴는 null — 한 메뉴가 가격 행마다 별도 항목으로 온다 */
  priceName: string | null;
  /** 현재 앱에 노출 중인 배달가 — 요청 매장가와 비교하는 기준값 */
  deliveryPrice: number;
  /** 점주가 이번 요청으로 인증받으려는 매장가 */
  storePrice: number;
  /** true면 승인 시 픽업가도 이 매장가와 동일하게 설정된다(PDF: '픽업가격 동일 설정') */
  applyPickupSamePrice: boolean;
}

/**
 * 매장가격 인증 요청 목록 한 건.
 *
 * <p>검수의 핵심 근거는 점주가 올린 매장 가격표 이미지다 — 이미지 없이는 요청 내용이
 * 실제 매장 가격과 일치하는지 확인할 방법이 없으므로, 이미지 검수 탭과 마찬가지로
 * 이미지가 없거나 로드에 실패하면 승인을 막는다.
 *
 * <p><b>대상 메뉴는 개수만 갖는다.</b> 요청 1건에 메뉴가 N건 달려 목록에 펼치면 페이징이 깨지므로,
 * 메뉴별 배달가·매장가 대조는 {@link StorePriceVerificationRequestDetail} 로 따로 조회한다.
 */
export interface StorePriceVerificationRequestItem {
  id: number;
  shopId: number;
  shopName: string;
  /** 검수 근거인 매장 가격표 이미지 URL. 없으면 null */
  priceListImageUrl: string | null;
  status: StorePriceVerificationStatus;
  rejectReason: string | null;
  /** 이 요청에 딸린 대상 메뉴 수 */
  itemCount: number;
}

/** 매장가격 인증 요청 상세 — 판정 근거인 메뉴별 앱 배달가 대 요청 매장가를 담는다 */
export interface StorePriceVerificationRequestDetail {
  id: number;
  shopId: number;
  shopName: string;
  priceListImageUrl: string | null;
  status: StorePriceVerificationStatus;
  rejectReason: string | null;
  items: StorePriceVerificationRequestTargetItem[];
}
