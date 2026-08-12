import type {
  ApprovalStatus,
  ContentBoardTopic,
  ContentBoardType,
  DayType,
  DeliveryAreaAdjustmentStatus,
  DeliveryTipSurchargeUnit,
  ExtraDeliveryTipType,
  HygieneBadgeType,
  ImageType,
  OrderMethod,
  OrderUnavailableReason,
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
  DeliveryAreaAdjustmentStatus,
  DeliveryTipSurchargeUnit,
  ExtraDeliveryTipType,
  HygieneBadgeType,
  ImageType,
  OrderMethod,
  OrderUnavailableReason,
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

/**
 * 배달가능지역의 등록 출처.
 *
 * `MANUAL` 은 행정동을 직접 고르거나 반경으로 일괄 추가한 행, `POLYGON` 은 지도에 그린 도형을
 * 서버가 행정동으로 환산해 만든 행이다. 폴리곤 저장은 `POLYGON` 행만 통째로 교체하므로
 * `MANUAL` 행은 그대로 남는다.
 */
export type DeliveryAreaSource = "MANUAL" | "POLYGON";

export interface ShopDeliveryArea {
  id: number;
  adminDongId: number;
  regionName: string;
  source: DeliveryAreaSource;
}

/** 배달가능지역 등록 후보 — 행정동 검색 결과 */
export interface AdminDong {
  id: number;
  code: string;
  /** 서버가 완성해 내려주는 행정동 전체 이름 */
  regionName: string;
}

// ===== 배달지역 지도 편집 =====

/** 위경도 한 점. API 경계에서는 GeoJSON 의 `[lng, lat]` 순서 혼동을 피해 객체로 주고받는다 */
export interface GeoPoint {
  latitude: number;
  longitude: number;
}

/** 폴리곤의 링 하나 — 첫 점과 끝 점이 같을 필요는 없다(서버가 자동 폐합한다) */
export type GeoRing = GeoPoint[];

/** 행정동 계층 조회의 단계 */
export type AdminDongTreeLevel = "SIDO" | "SIGUNGU" | "DONG";

/** 행정동 계층 노드 한 건. `adminDongId`/`code` 는 `DONG` 레벨에서만 채워진다 */
export interface AdminDongTreeNode {
  name: string;
  adminDongId: number | null;
  code: string | null;
  /** 하위 행정동 수. `DONG` 레벨에서는 1 */
  dongCount: number;
}

/** 행정동 계층 조회 결과 — 어떤 레벨의 목록인지와 항목들 */
export interface AdminDongTree {
  level: AdminDongTreeLevel;
  items: AdminDongTreeNode[];
}

/** 행정동 경계 한 건. 경계 데이터 미보유는 오류가 아니라 `rings: null` 인 정상 상태다 */
export interface AdminDongBoundary {
  adminDongId: number;
  regionName: string;
  /** 대표점(경계 내부 보장점). centroid 가 아니다 */
  centerLatitude: number;
  centerLongitude: number;
  rings: GeoRing[] | null;
}

/** 뷰포트 경계 조회 결과. bbox 가 너무 넓으면 `truncated: true` 로 빈 목록을 준다 */
export interface AdminDongBoundaryResult {
  truncated: boolean;
  items: AdminDongBoundary[];
}

/** 반경 미리보기에 걸린 행정동 한 건 */
export interface DeliveryAreaRadiusCandidate {
  adminDongId: number;
  regionName: string;
  centerLatitude: number;
  centerLongitude: number;
  /** 이미 배달가능지역으로 등록돼 있는지 */
  alreadyRegistered: boolean;
}

/** 반경 미리보기 결과 */
export interface DeliveryAreaRadiusPreview {
  centerLatitude: number;
  centerLongitude: number;
  radiusMeters: number;
  maxAllowedRadiusMeters: number;
  defaultExposureRadiusMeters: number;
  /** 72각형으로 근사한 원. 클라이언트가 draft 폴리곤에 union 할 재료로 쓴다 */
  circle: GeoPoint[];
  adminDongs: DeliveryAreaRadiusCandidate[];
  adminDongCount: number;
  /** 좌표·경계를 갖고 있지 않아 판정하지 못한 행정동 수 */
  unresolvedCount: number;
}

/** bulk 추가·삭제·반경 적용의 반영 결과 */
export interface DeliveryAreaBulkOutcome {
  requestedCount: number;
  addedCount: number;
  /** 이미 등록돼 있어 건너뛴 개수. 중복은 실패가 아니라 skip 이다 */
  skippedCount: number;
  removedCount: number;
  /** 반영 후 이 가게의 총 배달가능지역 개수 */
  totalCount: number;
}

/** 저장된 배달지역 도형. 미설정은 오류가 아니라 `exists: false` 인 정상 상태다 */
export interface DeliveryAreaPolygon {
  exists: boolean;
  rings: GeoRing[] | null;
  /** 저장 시점의 가게 좌표 스냅샷 — 7km 상한의 기준점 */
  centerLatitude: number | null;
  centerLongitude: number | null;
  /** 현재 가게 좌표 */
  shopLatitude: number;
  shopLongitude: number;
  /** 0 보다 크면 가게 주소가 이전된 것 — 도형 재설정을 안내한다 */
  centerMovedMeters: number;
  maxRadiusMeters: number | null;
  maxAllowedRadiusMeters: number;
  defaultExposureRadiusMeters: number;
  ringCount: number | null;
  vertexCount: number | null;
  /** 이 도형에서 환산된 `source='POLYGON'` 행정동 수 */
  projectedAdminDongCount: number;
  updatedAt: string | null;
}

/** 환산 미리보기에 등장하는 행정동 한 건 */
export interface DeliveryAreaPolygonCandidate {
  adminDongId: number;
  regionName: string;
  alreadyRegistered: boolean;
}

/** 배달팁 참조 때문에 닫을 수 없는 행정동 */
export interface DeliveryAreaBlockedCandidate {
  adminDongId: number;
  regionName: string;
  /** 현재는 `"REGION_TIP"` 한 가지 */
  reason: string;
}

/**
 * 폴리곤 저장 전 환산 미리보기.
 *
 * `blockedAdminDongs` 를 저장 전에 알려주므로 점주가 409 를 맞기 전에 배달팁을 정리할 수 있다.
 */
export interface DeliveryAreaPolygonPreview {
  maxRadiusMeters: number;
  withinAllowedRadius: boolean;
  adminDongs: DeliveryAreaPolygonCandidate[];
  addedAdminDongs: DeliveryAreaPolygonCandidate[];
  removedAdminDongs: DeliveryAreaPolygonCandidate[];
  blockedAdminDongs: DeliveryAreaBlockedCandidate[];
  unresolvedCount: number;
}

/**
 * 배달지역 조정 신청 이력 한 건.
 *
 * 승인(COMPLETED)은 조정 성립 사실의 기록일 뿐이며 배달가능지역이 자동 반영되지는 않는다.
 * 실제 반영은 배달가능지역 등록/삭제로 별도 수행한다.
 */
export interface DeliveryAreaAdjustmentRequest {
  id: number;
  counterpartShopName: string;
  counterpartBusinessNumber: string;
  franchiseName: string;
  reason: string;
  /** 동의서 표시용 URL. 없으면 null */
  consentFileUrl: string | null;
  status: DeliveryAreaAdjustmentStatus;
  /** 반려 사유. 반려가 아니면 null */
  rejectReason: string | null;
  createdAt: string;
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

export interface ShopRiderPickupLocation {
  roadAddress: string;
  lotAddress: string | null;
  detailAddress: string | null;
  latitude: number;
  longitude: number;
}

export interface ShopRiderGuide {
  /** 미등록 시 null */
  visitGuide: string | null;
  /** 미설정 시 null — 라이더에게는 가게 실주소가 폴백으로 안내된다 */
  pickupLocation: ShopRiderPickupLocation | null;
  /** 픽업 위치 미설정 시 폴백으로 쓰이는 가게 실주소 (참고 표시용) */
  shopRoadAddress: string;
  shopLotAddress: string | null;
  shopLatitude: number;
  shopLongitude: number;
  updatedAt: string | null;
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
  /** 라이더 앱에만 노출되는 방문 안내·픽업 위치. 고객에게는 표시하지 않는다 */
  riderGuide: ShopRiderGuide;
}

export interface ShopOrderMethodAvailability {
  orderMethod: OrderMethod;
  /** 서버가 내려주는 주문유형 한글명. 프론트에서 코드를 라벨로 변환하지 않는다 */
  orderMethodName: string;
  orderable: boolean;
  /** 화면에 표시하지 않고 향후 사유별 분기에 사용한다 */
  unavailableReason: OrderUnavailableReason | null;
  /** 서버가 완성해 내려주는 한글 사유 문구 — 그대로 렌더한다 */
  unavailableReasonName: string | null;
}

export interface ShopOrderAvailability {
  orderable: boolean;
  unavailableReason: OrderUnavailableReason | null;
  unavailableReasonName: string | null;
  /** 배정된 주문유형별 상태. 배정이 없으면 빈 배열 */
  orderMethods: ShopOrderMethodAvailability[];
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

/**
 * 가게 변경이력 1건.
 *
 * `previousValue`/`newValue` 는 서버가 사람이 읽는 형태로 굳혀 내려주는 요약 문자열이며
 * 줄바꿈을 포함할 수 있다 — 프론트에서 파싱하지 않고 `whitespace-pre-line` 으로 렌더한다.
 * `previousValue` 가 null 이면 등록(CREATE), `newValue` 가 null 이면 삭제(DELETE)다.
 */
export interface ShopChangeHistoryItem {
  id: number;
  category: string;
  categoryName: string;
  changeType: string;
  changeTypeName: string;
  actionType: string;
  actionTypeName: string;
  previousValue: string | null;
  newValue: string | null;
  changedAt: string;
}

/** 변경이력 필터 중분류 옵션 — 서버 카탈로그가 내려주는 코드·한글 라벨 */
export interface ShopChangeTypeOption {
  code: string;
  name: string;
}

/** 변경이력 필터 대분류 옵션. 자기 하위 중분류를 보유한다 */
export interface ShopChangeCategoryOption {
  code: string;
  name: string;
  changeTypes: ShopChangeTypeOption[];
}

/**
 * 요청처리 현황 목록의 요청 1건.
 *
 * `requestType`·`status` 를 리터럴 유니온으로 좁히지 않고 `string` 으로 둔다 —
 * 라벨과 필터 옵션을 서버 카탈로그에서 받으므로 프론트가 값 목록을 알 필요가 없고,
 * 백엔드에 유형이 추가돼도 이 타입을 고치지 않아도 된다.
 * 값을 아는 곳은 배지 variant 매핑(`@/components/status-badge`) 하나뿐이다.
 */
export interface ShopRequestListItem {
  /** 인덱스 행 ID. 상세·취소·문의의 유일한 식별자 */
  requestId: number;
  requestType: string;
  requestTypeDescription: string;
  /** 무엇을 요청했는지 서버가 굳혀 내려주는 한 줄 요약 */
  summary: string;
  status: string;
  statusDescription: string;
  /** 반려 사유. REJECTED 일 때만 채워진다 */
  rejectReason: string | null;
  /** 전자계약서가 수정되는 요청인지 */
  contractAmending: boolean;
  hasAttachment: boolean;
  commentCount: number;
  requestedAt: string;
  /** 최근 처리 일시. 접수 직후에는 null */
  processedAt: string | null;
}

/** 이미지 변경 요청의 유형별 상세 블록 */
export interface ShopRequestImageChange {
  imageType: string;
  imageTypeDescription: string;
  imageUrl: string;
}

/** 배달지역 조정 신청의 유형별 상세 블록 */
export interface ShopRequestAdjustment {
  counterpartShopName: string;
  counterpartBusinessNumber: string;
  franchiseName: string;
  reason: string;
  consentFileUrl: string;
}

/**
 * 요청 상세. 목록의 전 필드에 첨부와 유형별 블록이 더해진다.
 *
 * `imageChange`/`deliveryAreaAdjustment` 는 둘 중 하나만 채워지며 어느 쪽인지는
 * `requestType` 이 결정한다(다형 응답 대신 nullable 서브 객체 — `docs/tasks/backend.md` 4-2).
 */
export interface ShopRequestDetail extends ShopRequestListItem {
  attachmentLabel: string | null;
  attachmentUrl: string | null;
  imageChange: ShopRequestImageChange | null;
  deliveryAreaAdjustment: ShopRequestAdjustment | null;
}

/**
 * 요청 문의 스레드의 댓글 1건.
 *
 * 작성자 실명은 서버가 내려주지 않는다 — 화면은 작성자 유형 라벨로만 구성한다.
 */
export interface ShopRequestComment {
  commentId: number;
  authorType: string;
  authorTypeDescription: string;
  content: string;
  createdAt: string;
}

/** 요청 유형 필터 옵션 — 서버 카탈로그가 내려주는 코드·한글 라벨 */
export interface ShopRequestTypeOption {
  code: string;
  description: string;
  contractAmending: boolean;
}

/** 처리 상태 필터 옵션 — 서버 카탈로그가 내려주는 코드·한글 라벨 */
export interface ShopRequestStatusOption {
  code: string;
  description: string;
}
