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
  createdAt: string;
  updatedAt: string;
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
