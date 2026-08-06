// 점주용 가게 관리 API 요청/응답 DTO (Shop CEO — /api/shops)
// DTO 는 이 계층 밖으로 나가지 않는다. UI/feature 는 @/feature/shop/domain 을 사용한다.
//
// 확정된 배민 사장님 셀프서비스 API 스펙(docs/CEO-API-SHOP-SPEC-FOR-FRONTEND.md)을
// 유일한 근거로 작성한다. 스펙이 바뀌면 이 파일과 shop.repository.ts 두 곳을 갱신한다.

// ===== 가게 본체 =====

// 내 가게 목록 조회 쿼리
export interface ShopListQueryRequest {
  name?: string;
  stationId?: number;
  permanentlyClosed?: boolean;
}

// 내 가게 목록 항목
export interface ShopListItemResponse {
  id: number;
  name: string;
  stationName: string;
  roadAddress: string;
  permanentlyClosed: boolean;
}

// 가게 상세
export interface ShopDetailResponse {
  id: number;
  stationId: number;
  name: string;
  latitude: number;
  longitude: number;
  roadAddress: string;
  lotAddress: string;
  phoneNumber: string;
  thumbnailImageUrl: string | null;
  trademarkImageUrl: string | null;
  permanentlyClosed: boolean;
  hidden: boolean;
  closedOnPublicHolidays: boolean;
  /** 최소주문금액. 0이면 미설정(제한 없음)이며, 배달 주문에만 적용된다. */
  minOrderAmount: number;
}

// ===== 최소주문금액 =====

export interface ShopMinOrderAmountUpdateRequest {
  minOrderAmount: number;
}

// ===== 배달팁 =====

/** 추가 배달팁 방식 — 거리별과 지역별은 상호 배타이므로 서버가 단일 값으로 내려준다 */
export type ExtraDeliveryTipType = "NONE" | "DISTANCE" | "REGION";

/** 추가 거리 할증 단위 */
export type DeliveryTipSurchargeUnit = "PER_100M" | "PER_500M";

export interface ShopDeliveryTipTierItemResponse {
  id: number;
  tierOrder: number;
  minOrderAmount: number;
  tipAmount: number;
}

export interface ShopDeliveryTipDistanceResponse {
  baseDistanceMeters: number;
  surchargeUnit: DeliveryTipSurchargeUnit;
  surchargeAmount: number;
}

export interface ShopDeliveryTipRegionItemResponse {
  id: number;
  adminDongId: number;
  /** "서울특별시 강남구 역삼1동" 형태로 서버가 완성해서 내려준다 — 프론트가 조립하지 않는다 */
  regionName: string;
  tipAmount: number;
}

export interface ShopDeliveryTipScheduleItemResponse {
  id: number;
  dayType: DayType;
  /** "HH:mm:ss" */
  startTime: string;
  /** "HH:mm:ss" */
  endTime: string;
  tipAmount: number;
}

export interface ShopDeliveryTipSettingResponse {
  tiers: ShopDeliveryTipTierItemResponse[];
  extraTipType: ExtraDeliveryTipType;
  distance: ShopDeliveryTipDistanceResponse | null;
  regions: ShopDeliveryTipRegionItemResponse[];
  schedules: ShopDeliveryTipScheduleItemResponse[];
  /** 0이면 미설정 */
  holidayTipAmount: number;
}

export interface ShopDeliveryAreaItemResponse {
  id: number;
  adminDongId: number;
  regionName: string;
}

export interface ShopDeliveryTipTierItemRequest {
  minOrderAmount: number;
  tipAmount: number;
}

/** 구간 저장은 replace-all — 단조성 불변식이 집합 전체를 봐야 판정된다 */
export interface ShopDeliveryTipTiersUpdateRequest {
  tiers: ShopDeliveryTipTierItemRequest[];
}

export interface ShopDeliveryTipDistanceUpdateRequest {
  baseDistanceMeters: number;
  surchargeUnit: DeliveryTipSurchargeUnit;
  surchargeAmount: number;
}

export interface ShopDeliveryTipRegionItemRequest {
  adminDongId: number;
  tipAmount: number;
}

export interface ShopDeliveryTipRegionsUpdateRequest {
  regions: ShopDeliveryTipRegionItemRequest[];
}

export interface ShopDeliveryTipScheduleItemRequest {
  dayType: DayType;
  startTime: string;
  endTime: string;
  tipAmount: number;
}

export interface ShopDeliveryTipSchedulesUpdateRequest {
  schedules: ShopDeliveryTipScheduleItemRequest[];
}

export interface ShopDeliveryTipHolidayUpdateRequest {
  /** 0이면 해제 */
  tipAmount: number;
}

export interface ShopDeliveryAreaCreateRequest {
  adminDongId: number;
}

// ===== 영업시간 · 휴게시간 =====

// dayType — 스펙 §11 enum 참조 카탈로그 기준
export type DayType =
  | "DAILY"
  | "WEEKDAY"
  | "WEEKEND"
  | "HOLIDAY"
  | "MONDAY"
  | "TUESDAY"
  | "WEDNESDAY"
  | "THURSDAY"
  | "FRIDAY"
  | "SATURDAY"
  | "SUNDAY";

export interface BusinessHourResponse {
  id: number;
  dayType: DayType;
  /** 서버가 내려주는 한글 라벨 (예: "평일") */
  description: string;
  openTime: string;
  closeTime: string;
  isClosed: boolean;
  /** 24시간 영업 — true 면 openTime/closeTime 은 무시된다 */
  is24Hours: boolean;
}

export interface BusinessHourCreateRequest {
  dayType: DayType;
  openTime: string;
  closeTime: string;
  isClosed: boolean;
  is24Hours: boolean;
}

export interface BusinessHourUpdateRequest {
  dayType: DayType;
  openTime: string;
  closeTime: string;
  isClosed: boolean;
  is24Hours: boolean;
}

export interface BreakTimeResponse {
  id: number;
  dayType: DayType;
  /** 서버가 내려주는 한글 라벨 (예: "평일") */
  description: string;
  startTime: string;
  endTime: string;
}

export interface BreakTimeCreateRequest {
  dayType: DayType;
  startTime: string;
  endTime: string;
}

export interface BreakTimeUpdateRequest {
  dayType: DayType;
  startTime: string;
  endTime: string;
}

// ===== 휴무일 (공휴일 · 정기 · 임시 통합) =====

// closedDayType — 스펙 §11 예시 기준(EVERY_WEEK_*, EVERY_MONTH_*_WEEK_*). 정확한 후보값은 Swagger 확인.
export type ClosedDayType = string;

export interface RegularClosedDayResponse {
  id: number;
  closedDayType: ClosedDayType;
  /** 서버가 내려주는 한글 라벨 (예: "매주 월요일") */
  description: string;
}

export interface TemporaryClosureResponse {
  id: number;
  startDate: string;
  endDate: string;
}

// GET /{id}/closed-days 통합 응답
export interface ShopClosedDaysResponse {
  closedOnPublicHolidays: boolean;
  regularClosedDays: RegularClosedDayResponse[];
  temporaryClosures: TemporaryClosureResponse[];
}

export interface ClosedDayCreateRequest {
  closedDayType: ClosedDayType;
}

export interface HolidayClosedUpdateRequest {
  closedOnPublicHolidays: boolean;
}

export interface TemporaryClosureCreateRequest {
  startDate: string;
  endDate: string;
}

// ===== 가게 전화번호 =====

export interface PhoneNumberResponse {
  id: number;
  phoneNumber: string;
  primary: boolean;
  virtual: boolean;
}

export interface PhoneNumberCreateRequest {
  phoneNumber: string;
  virtual: boolean;
}

// ===== 가게 상태 =====

export interface ShopStatusResponse {
  hidden: boolean;
  permanentlyClosed: boolean;
}

export type ShopStatusValue = "OPEN" | "HIDDEN";

export interface ShopStatusUpdateRequest {
  status: ShopStatusValue;
}

// ===== 가게 소개 =====

export interface ShopIntroductionResponse {
  message: string | null;
}

export interface ShopIntroductionUpdateRequest {
  message: string;
}

export interface ShopIntroductionValidateResponse {
  valid: boolean;
  violations: string[];
}

// ===== 편의정보 · 찾아오는 길 · 노출 위치 =====

export interface ShopConvenienceInfoResponse {
  id: number;
  shopId: number;
  parkingAvailable: boolean;
  parkingPaid: boolean;
  valetAvailable: boolean;
  valetPaid: boolean;
  directionsGuide: string | null;
  displayLatitude: number;
  displayLongitude: number;
}

export interface ShopConvenienceInfoUpdateRequest {
  parkingAvailable: boolean;
  parkingPaid: boolean;
  valetAvailable: boolean;
  valetPaid: boolean;
  directionsGuide: string;
  displayLatitude: number;
  displayLongitude: number;
}

// ===== 편의시설(기타) =====

// amenity — 스펙에는 후보값이 명시되지 않아 서버 카테고리 응답의 원문 문자열을 그대로 쓴다.
export type Amenity = string;

export interface AmenityCategoryResponse {
  id: number;
  amenity: Amenity;
  displayName: string;
  activeFilePath: string;
}

export interface ShopAmenityResponse {
  id: number;
  amenityCategoryId: number;
  amenity: Amenity;
  displayName: string;
  activeFilePath: string;
}

export interface AmenityCreateRequest {
  amenityCategoryId: number;
}

// ===== 상표 · 대표이미지 (승인 워크플로) =====

export type ImageType = "TRADEMARK" | "THUMBNAIL";
export type ApprovalStatus = "PENDING" | "APPROVED" | "REJECTED";

export interface ImageChangeRequestResponse {
  id: number;
  imageType: ImageType;
  imageUrl: string;
  status: ApprovalStatus;
  rejectReason: string | null;
}

export interface ShopImageStatusResponse {
  currentImageUrl: string | null;
  requests: ImageChangeRequestResponse[];
}

// ===== 콘텐츠보드 =====

export type ContentBoardType = "IMAGE" | "GIF" | "VIDEO";
export type ContentBoardTopic = "EXTERIOR" | "INTERIOR" | "FOOD_STORY" | "NEWS";

export interface ContentBoardResponse {
  id: number;
  shopId: number;
  contentType: ContentBoardType;
  topic: ContentBoardTopic;
  imageUrl: string | null;
  youtubeUrl: string | null;
  description: string;
  hidden: boolean;
}

// 등록/수정은 multipart/form-data — repository 에서 FormData 로 구성한다.
export interface ContentBoardMutationFields {
  contentType: ContentBoardType;
  topic: ContentBoardTopic;
  youtubeUrl?: string;
  description: string;
}

// ===== 영업 임시중지 ('준비중') =====

// orderMethod — 스펙 §9/§11 기준
export type OrderMethod = "TABLE" | "RESERVATION" | "DELIVERY" | "TAKEOUT";

export type SuspensionReason =
  | "EARLY_CLOSE"
  | "OPEN_DELAY"
  | "SHOP_CIRCUMSTANCE"
  | "UNREACHABLE"
  | "TERMINATION_REQUEST"
  | "BAD_WEATHER";

export interface SuspensionResponse {
  id: number;
  shopId: number;
  reason: SuspensionReason;
  /** orderMethods 를 지정하지 않고 등록하면(전체 주문유형 대상) 서버가 null 을 내려준다. */
  orderMethod: OrderMethod | null;
  startAt: string;
  endAt: string;
  releasedAt: string | null;
}

// 단건 등록 — orderMethods 를 비우면 전체 주문유형 대상. 여러 개 지정 시 유형별로 각 1건씩 생성된다.
export interface SuspensionCreateRequest {
  reason: SuspensionReason;
  orderMethods: OrderMethod[];
  startAt: string;
  endAt: string;
}

// 일괄 등록 — 내 소유 가게만 대상. shopId 하위 경로가 아니다.
export interface SuspensionBulkCreateRequest {
  shopIds: number[];
  reason: SuspensionReason;
  orderMethods: OrderMethod[];
  startAt: string;
  endAt: string;
}

// ===== 위생 인증 (조회 전용) =====

export type HygieneBadgeType = "FOOD_SAFETY_CERTIFIED" | "CESCO_BLUE" | "CESCO_WHITE";

export interface HygieneBadgeResponse {
  id: number;
  shopId: number;
  badgeType: HygieneBadgeType;
  certifiedDate: string;
  lastInspectionMonth: string;
}
