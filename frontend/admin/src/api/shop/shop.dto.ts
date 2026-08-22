// 가게 관리자 API 요청/응답 DTO (Shop Admin — /api/shops)
// DTO 는 이 계층 밖으로 나가지 않는다. UI/feature 는 @/feature/shop/domain 을 사용한다.

// ===== Phase A. 가게 본체 CRUD =====

// 지하철역 (등록/수정 폼 드롭다운용)
export interface StationResponse {
  id: number;
  stationName: string;
}

// 점주(ceo) — 가게 등록 폼 소유 점주 선택 드롭다운용. 페이징 없음(전체 목록).
export interface CeoResponse {
  id: number;
  name: string;
  businessRegistrationNumber: string;
  status: "ACTIVE" | "INACTIVE";
}

// 가게 목록 조회 쿼리
export interface ShopListQueryRequest {
  name?: string;
  stationId?: number;
  permanentlyClosed?: boolean;
}

// 가게 목록 항목
export interface ShopListItemResponse {
  id: number;
  name: string;
  stationName: string;
  roadAddress: string;
  rating: number | null;
  permanentlyClosed: boolean;
}

// 가게 등록
export interface ShopCreateRequest {
  ceoId?: number;
  stationId: number;
  name: string;
  latitude: number;
  longitude: number;
  roadAddress: string;
  lotAddress: string;
  phoneNumber?: string;
  thumbnailImageFileId?: number;
}

// 가게 수정 (등록과 동일 필드)
export interface ShopUpdateRequest {
  stationId: number;
  name: string;
  latitude: number;
  longitude: number;
  roadAddress: string;
  lotAddress: string;
  phoneNumber?: string;
  thumbnailImageFileId?: number;
}

// 가게 상세
export interface ShopDetailResponse {
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

// 일회용컵 보증금 대상사업자 토글
export interface ShopCupDepositUpdateRequest {
  enabled: boolean;
}

// ===== Phase B. 운영시간 · 휴게시간 · 정기휴무일 =====

// dayType — 백엔드 DayType enum 기준(2026-07-19 확인)
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

// closedDayType — 백엔드 ClosedDayType enum 기준(constants.ts CLOSED_DAY_TYPE_OPTIONS 참조, 2026-07-19 확인)
export type ClosedDayType = string;

export interface ClosedDayResponse {
  id: number;
  closedDayType: ClosedDayType;
  /** 서버가 내려주는 한글 라벨 (예: "매주 월요일") */
  description: string;
}

export interface ClosedDayCreateRequest {
  closedDayType: ClosedDayType;
}

// ===== Phase C. 편의시설 · 음식종류 · 태그 =====

// amenity 허용값 초안(WIFI, PARKING, PET_FRIENDLY 등) — 백엔드 enum과 대조 확인 필요
export type Amenity = string;

export interface AmenityCategoryResponse {
  id: number;
  amenity: Amenity;
  displayName: string;
  activeImageUrl: string;
  inactiveImageUrl: string;
  sort: number;
  visible: boolean;
}

export interface AmenityCategoryCreateRequest {
  amenity: Amenity;
  displayName: string;
  activeImageFileId: number;
  inactiveImageFileId: number;
  sort: number;
  visible: boolean;
}

export interface AmenityCategoryUpdateRequest {
  amenity: Amenity;
  displayName: string;
  activeImageFileId: number;
  inactiveImageFileId: number;
  sort: number;
  visible: boolean;
}

// foodType 허용값 초안(KOREAN, JAPANESE, CHINESE, WESTERN 등) — 백엔드 enum과 대조 확인 필요
export type FoodType = string;

export interface FoodTypeCategoryResponse {
  id: number;
  foodType: FoodType;
  displayName: string;
  activeImageUrl: string;
  inactiveImageUrl: string;
  sort: number;
  visible: boolean;
}

export interface FoodTypeCategoryCreateRequest {
  foodType: FoodType;
  displayName: string;
  activeImageFileId: number;
  inactiveImageFileId: number;
  sort: number;
  visible: boolean;
}

export interface FoodTypeCategoryUpdateRequest {
  foodType: FoodType;
  displayName: string;
  activeImageFileId: number;
  inactiveImageFileId: number;
  sort: number;
  visible: boolean;
}

// 가게별 편의시설 지정 항목 (마스터 카테고리 참조)
export interface ShopAmenityResponse {
  id: number;
  amenityCategoryId: number;
  amenity: Amenity;
  displayName: string;
  activeFilePath: string;
}

export interface ShopAmenityCreateRequest {
  amenityCategoryId: number;
}

// 가게별 음식종류 지정 항목 (마스터 카테고리 참조)
export interface ShopFoodTypeResponse {
  id: number;
  foodTypeCategoryId: number;
  foodType: FoodType;
  displayName: string;
  activeFilePath: string;
}

export interface ShopFoodTypeCreateRequest {
  foodTypeCategoryId: number;
}

export interface TagResponse {
  id: number;
  tagName: string;
}

export interface TagCreateRequest {
  tagName: string;
}

// ===== Phase D. 주문수단 =====

// orderMethod — 백엔드 OrderMethod enum 기준(constants.ts ORDER_METHOD_OPTIONS 참조, 2026-07-19 확인)
export type OrderMethod = string;

export interface OrderMethodResponse {
  orderMethod: OrderMethod;
  /** 서버가 내려주는 한글 라벨 (예: "테이블 오더") */
  displayName: string;
}

export interface OrderMethodCreateRequest {
  orderMethod: OrderMethod;
}

// ===== Phase E. 배너 · 포토 이미지 =====

export interface BannerImageResponse {
  id: number;
  imageUrl: string;
  sort: number;
}

export interface BannerImageCreateRequest {
  imageFileId: number;
  sort: number;
}

export interface PhotoCategoryResponse {
  id: number;
  name: string;
}

export interface PhotoCategoryCreateRequest {
  name: string;
}

export interface PhotoCategoryUpdateRequest {
  name: string;
}

export interface PhotoImageResponse {
  id: number;
  imageUrl: string;
  sort: number;
  visible: boolean;
}

export interface PhotoImageCreateRequest {
  imageFileId: number;
  sort: number;
  visible: boolean;
}

export interface PhotoImageUpdateRequest {
  imageFileId: number;
  sort: number;
  visible: boolean;
}

// ===== Phase F. 테하 초이스 (큐레이션) =====

export interface EditorChoiceListQueryRequest {
  shopId?: number;
}

export interface EditorChoiceResponse {
  id: number;
  shopId: number;
  shopName: string;
  title: string;
  content: string;
  createdAt: string;
  updatedAt: string;
}

export interface EditorChoiceCreateRequest {
  shopId: number;
  title: string;
  content: string;
}

export interface EditorChoiceUpdateRequest {
  title: string;
  content: string;
}

// ===== Phase G. 이미지 변경요청 검수 (상표·대표이미지) =====

export type ShopImageType = "TRADEMARK" | "THUMBNAIL";

export type ShopImageChangeStatus = "PENDING" | "APPROVED" | "REJECTED";

export interface ShopImageChangeRequestListQueryRequest {
  status?: ShopImageChangeStatus;
  imageType?: ShopImageType;
}

export interface ShopImageChangeRequestItemResponse {
  id: number;
  shopId: number;
  imageType: ShopImageType;
  /** 미리보기 URL. 없으면 null */
  imageUrl: string | null;
  status: ShopImageChangeStatus;
  rejectReason: string | null;
}

export interface ShopImageChangeRejectRequest {
  reason: string;
}

// ===== 배달지역 조정 신청 검수 =====

export type DeliveryAreaAdjustmentStatus = "PENDING" | "IN_PROGRESS" | "COMPLETED" | "REJECTED";

export interface DeliveryAreaAdjustmentListQueryRequest {
  status?: DeliveryAreaAdjustmentStatus;
  shopId?: number;
}

export interface DeliveryAreaAdjustmentItemResponse {
  id: number;
  shopId: number;
  shopName: string;
  counterpartShopName: string;
  franchiseName: string;
  status: DeliveryAreaAdjustmentStatus;
  createdAt: string;
}

export interface DeliveryAreaAdjustmentDetailResponse extends DeliveryAreaAdjustmentItemResponse {
  counterpartBusinessNumber: string;
  reason: string;
  /** 동의서 표시용 URL. 없으면 null */
  consentFileUrl: string | null;
  /** 반려 사유. 반려가 아니면 null */
  rejectReason: string | null;
  updatedAt: string;
}

/** 전이 가능한 상태는 IN_PROGRESS · COMPLETED 뿐이다 */
export interface DeliveryAreaAdjustmentStatusUpdateRequest {
  status: DeliveryAreaAdjustmentStatus;
}

export interface DeliveryAreaAdjustmentRejectRequest {
  reason: string;
}

// ===== Phase H. 콘텐츠보드 검수 =====

export type ContentBoardContentType = "IMAGE" | "GIF" | "VIDEO";

export type ContentBoardTopic = "EXTERIOR" | "INTERIOR" | "FOOD_STORY" | "NEWS";

export interface ContentBoardListQueryRequest {
  shopId?: number;
  hidden?: boolean;
  contentType?: ContentBoardContentType;
}

export interface ContentBoardItemResponse {
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

export interface ShopContentBoardHideRequest {
  hidden: boolean;
}

// ===== Phase I. 위생 인증 뱃지 =====

export type HygieneBadgeType = "FOOD_SAFETY_CERTIFIED" | "CESCO_BLUE" | "CESCO_WHITE";

export interface ShopHygieneBadgeResponse {
  id: number;
  shopId: number;
  badgeType: HygieneBadgeType;
  /** LocalDate (YYYY-MM-DD) */
  certifiedDate: string;
  /** 세스코 최근 점검월 (YYYY-MM), 없으면 null */
  lastInspectionMonth: string | null;
}

export interface ShopHygieneBadgeCreateRequest {
  badgeType: HygieneBadgeType;
  certifiedDate: string;
  lastInspectionMonth?: string;
}

// ===== 주문안내 게시중단 =====

export interface ShopOrderNoticeResponse {
  content: string | null;
  hidden: boolean;
  hiddenReason: string | null;
}

export interface ShopOrderNoticeHideRequest {
  reason: string;
}

// ===== 라이더 가게방문 안내 검수 =====

export type RiderGuideActorType = "CEO" | "ADMIN";
export type RiderGuideActionType = "UPDATE" | "REVISION_REQUEST" | "DELETION";

export interface ShopRiderGuideListQueryRequest {
  shopName?: string;
  /** true면 문구가 등록된 가게만, false면 픽업 위치만 설정된 가게만, 미지정 시 전체 */
  hasVisitGuide?: boolean;
}

export interface ShopRiderGuideListItemResponse {
  shopId: number;
  shopName: string;
  /** 미등록 시 null */
  visitGuide: string | null;
  hasPickupLocation: boolean;
  updatedAt: string;
}

export interface ShopRiderPickupLocationResponse {
  roadAddress: string;
  lotAddress: string | null;
  detailAddress: string | null;
  latitude: number;
  longitude: number;
}

export interface ShopRiderGuideHistoryResponse {
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

export interface ShopRiderGuideDetailResponse {
  shopId: number;
  shopName: string;
  shopRoadAddress: string;
  visitGuide: string | null;
  /** 미설정 시 null — 라이더에게는 가게 실주소가 폴백으로 안내된다 */
  pickupLocation: ShopRiderPickupLocationResponse | null;
  /** 최신순, 최대 20건 */
  histories: ShopRiderGuideHistoryResponse[];
}

export interface ShopRiderVisitGuideDeleteRequest {
  reason: string;
}

export interface ShopRiderVisitGuideRevisionRequest {
  reason: string;
}

export interface ShopRiderPickupLocationUpdateRequest {
  roadAddress: string;
  lotAddress: string | null;
  detailAddress: string | null;
  latitude: number;
  longitude: number;
}

// ===== 메뉴모음컷 검수 =====

/**
 * 메뉴모음컷 승인 상태 — backend ApprovalStatus enum 기준.
 *
 * 위 `ShopImageChangeStatus`(3값)와 달리 점주 취소분(CANCELED)까지 4값이므로 별도 유니온으로 둔다.
 */
export type MenuCollectionImageStatus = "PENDING" | "APPROVED" | "REJECTED" | "CANCELED";

// 메뉴모음컷 검수 목록 조회 쿼리 — status 미지정은 전체
export interface MenuCollectionImageRequestListQueryRequest {
  status?: MenuCollectionImageStatus;
}

// 메뉴모음컷 검수 목록 항목
export interface MenuCollectionImageRequestItemResponse {
  id: number;
  shopId: number;
  shopName: string;
  imageUrl: string | null;
  sort: number;
  status: MenuCollectionImageStatus;
  rejectReason: string | null;
}

// 메뉴모음컷 검수 반려
export interface MenuCollectionImageRejectRequest {
  rejectReason: string;
}

// ===== 매장가격 인증 검수 =====

/**
 * 매장가격 인증 요청 상태 — backend `SHOP_REQUEST_INDEX` 공용 상태값 기준.
 *
 * <p>사장님 추천 등 `ApprovalStatusValue`(4값)와 달리, 인증 요청은 검수 착수를 나타내는
 * `IN_PROGRESS`(검수 중)를 추가로 갖는다 — 검수 중에는 점주가 재요청할 수 없다는 규칙이
 * 이 상태에 근거하므로(backend.md §B-1) 별도 유니온으로 둔다.
 */
export type StorePriceVerificationStatus = "PENDING" | "IN_PROGRESS" | "APPROVED" | "REJECTED" | "CANCELED";

/**
 * 매장가격 인증 요청 목록 항목.
 *
 * <p><b>목록에는 대상 메뉴가 개수(`itemCount`)로만 담긴다.</b> 요청 1건에 메뉴가 N건 달려 있어
 * 목록에 펼치면 페이징이 깨지므로, 서버는 목록에 훑어보기용 값(가게·상태·항목 수·가격표 이미지)만
 * 내려주고 메뉴별 배달가·매장가 대조는 상세 조회
 * (`GET /api/shops/v1/store-price-verifications/{id}`)가 담당한다.
 */
export interface StorePriceVerificationRequestItemResponse {
  id: number;
  shopId: number;
  shopName: string;
  status: StorePriceVerificationStatus;
  /** 검수 근거인 매장 가격표 이미지 URL. 없으면 null — 근거 없이 승인할 수 없다 */
  priceListFileUrl: string | null;
  rejectReason: string | null;
  /** 이 요청에 딸린 대상 메뉴 수. 메뉴별 값은 상세 조회로 받는다 */
  itemCount: number;
  requestedAt: string;
  processedAt: string | null;
}

/**
 * 매장가격 인증 요청 상세.
 *
 * <p>검수자는 가격표 이미지(근거)와 항목별 배달가·매장가를 함께 봐야 "매장보다 앱 가격이 높은 메뉴"
 * 같은 반려 사유를 판단할 수 있으므로, 대상 메뉴는 이 상세 응답에만 담긴다.
 */
export interface StorePriceVerificationRequestDetailResponse {
  id: number;
  shopId: number;
  shopName: string;
  status: StorePriceVerificationStatus;
  priceListFileUrl: string | null;
  rejectReason: string | null;
  requestedAt: string;
  processedAt: string | null;
  items: StorePriceVerificationRequestTargetItemResponse[];
}

// 매장가격 인증 요청 대상 메뉴 한 건 — 앱 배달가와 요청 매장가를 나란히 대조하기 위한 값
export interface StorePriceVerificationRequestTargetItemResponse {
  productId: number;
  productName: string;
  /** 인증 대상 가격 행 id. 한 메뉴에 가격명이 여러 개면 행마다 별도 항목으로 내려온다 */
  priceId: number;
  /** 가격명(보통/곱빼기 등). 단일 가격 메뉴는 null */
  priceName: string | null;
  /** 점주가 이번 요청으로 인증받으려는 매장가 */
  storePrice: number;
  /** 현재 앱에 노출 중인 배달가 — 요청 매장가와 비교해 "앱 가격이 매장보다 높은지" 판단하는 기준값 */
  deliveryPrice: number;
  /** true면 승인 시 픽업가도 이 매장가와 동일하게 설정된다(PDF: '픽업가격 동일 설정') */
  applyPickupSamePrice: boolean;
}

// 매장가격 인증 요청 목록 조회 쿼리 — status 미지정은 전체
export interface StorePriceVerificationRequestListQueryRequest {
  status?: StorePriceVerificationStatus;
}

// 매장가격 인증 요청 반려 — 필드명은 사장님 추천 등 기존 검수와 동일하게 rejectReason 을 쓴다
export interface StorePriceVerificationRejectRequest {
  rejectReason: string;
}
