/**
 * 점주 품절·숨김 설정 DTO (`docs/tasks/backend.md` §3-3).
 *
 * DTO 는 이 계층을 벗어나지 않는다 — UI 는 `@/feature/product/domain` 의 타입만 import 한다.
 */

/** 일반 옵션(`PRODUCT_OPTION`) / 공통 옵션(`PRODUCT_COMMON_OPTION`) 갈래 */
export type ProductOptionType = "NORMAL" | "COMMON";

/** 해제 대상. `ALL` 은 품절·숨김이 섞인 선택을 한 번에 푼다 */
export type ProductReleaseTarget = "SOLD_OUT" | "HIDDEN" | "ALL";

export interface ProductAvailabilityItemResponse {
  id: number;
  name: string;
  originalPrice: number;
  discountPrice: number | null;
  imageUrl: string | null;
  soldOut: boolean;
  /** ISO-8601 LocalDateTime. null 이면 무기한 품절 또는 판매중 */
  soldOutUntil: string | null;
  visible: boolean;
  /** 사장님 추천 메뉴. 숨김 제약(`PRODUCT_LAST_REPRESENTATIVE_CANNOT_HIDE`) 안내에 쓰인다 */
  representative: boolean;
  sort: number;
}

export interface ProductAvailabilityGroupResponse {
  /** 카테고리 미지정 메뉴는 null */
  categoryId: number | null;
  categoryName: string | null;
  sort: number;
  products: ProductAvailabilityItemResponse[];
}

export interface ProductOptionAvailabilityItemResponse {
  id: number;
  /** id 만으로는 일반/공통을 구분할 수 없어 항목마다 함께 내려온다 */
  optionType: ProductOptionType;
  name: string;
  additionalPrice: number;
  soldOut: boolean;
  soldOutUntil: string | null;
  visible: boolean;
  sort: number;
}

export interface ProductOptionAvailabilityGroupResponse {
  optionGroupId: number;
  optionType: ProductOptionType;
  name: string;
  required: boolean;
  minSelect: number | null;
  maxSelect: number | null;
  /** 이 옵션그룹이 연결된 메뉴 이름들. 서버 query DAO 가 join 으로 완성해 내려준다 */
  linkedProductNames: string[];
  sort: number;
  options: ProductOptionAvailabilityItemResponse[];
}

/** 부분실패 항목. `message` 는 서버가 내려준 한국어 문구라 그대로 노출한다 */
export interface ProductAvailabilityFailureResponse {
  id: number;
  name: string;
  errorCode: string;
  message: string;
}

/**
 * 일괄 처리 결과. HTTP 200 이어도 `failed` 가 채워질 수 있다 —
 * 부분실패는 요청 실패가 아니므로 호출부가 `failed.length` 로 안내를 갈라야 한다.
 */
export interface ProductAvailabilityChangeResponse {
  succeededIds: number[];
  failed: ProductAvailabilityFailureResponse[];
}

// ===== 요청 =====

export interface ProductAvailabilitySearchRequest {
  shopId: number;
  keyword?: string;
  soldOutOnly?: boolean;
  hiddenOnly?: boolean;
}

/** 옵션 일괄 처리의 대상 지정. `optionId` 만으로는 테이블을 특정할 수 없다 */
export interface ProductOptionTargetRequest {
  optionId: number;
  optionType: ProductOptionType;
}

export interface ProductSoldOutRequest {
  shopId: number;
  productIds: number[];
  /** 미지정이면 서버가 다음 오픈 시각으로 채운다 — 클라이언트는 계산하지 않는다 */
  soldOutUntil?: string;
}

export interface ProductHiddenRequest {
  shopId: number;
  productIds: number[];
}

export interface ProductReleaseRequest {
  shopId: number;
  productIds: number[];
  target: ProductReleaseTarget;
}

export interface ProductSoldOutUntilRequest {
  shopId: number;
  productIds: number[];
  soldOutUntil: string;
}

export interface ProductOptionSoldOutRequest {
  shopId: number;
  options: ProductOptionTargetRequest[];
  soldOutUntil?: string;
}

export interface ProductOptionHiddenRequest {
  shopId: number;
  options: ProductOptionTargetRequest[];
}

export interface ProductOptionReleaseRequest {
  shopId: number;
  options: ProductOptionTargetRequest[];
  target: ProductReleaseTarget;
}

export interface ProductOptionSoldOutUntilRequest {
  shopId: number;
  options: ProductOptionTargetRequest[];
  soldOutUntil: string;
}

// =====================================================================================
// 점주 메뉴·옵션 관리 DTO (`docs/tasks/backend.md` §2~§7)
//
// 위 품절·숨김 DTO 와 같은 파일에 두는 이유는 같은 `/api/products` 리소스이기 때문이다.
// 여기서도 DTO 는 `src/api/` 를 벗어나지 않는다.
// =====================================================================================

/** 채식 단계. 서버 `VegetarianType` 과 값이 1:1 */
export type VegetarianType = "VEGAN" | "LACTO" | "OVO" | "LACTO_OVO" | "PESCO";

/** 노출되지 않는 사유. `exposedNow=false` 일 때만 채워진다 */
export type ProductHiddenReason =
  | "MANUALLY_HIDDEN"
  | "BEFORE_EXPOSURE_PERIOD"
  | "AFTER_EXPOSURE_PERIOD"
  | "OUT_OF_EXPOSURE_HOURS";

/** 승인 워크플로 상태(공용 `ApprovalStatus`) */
export type ApprovalStatus = "PENDING" | "APPROVED" | "REJECTED" | "CANCELED";

/**
 * 노출 요일. 묶음(`DAILY`~`HOLIDAY`)과 개별 요일을 한 enum 이 함께 담는다 —
 * 서버 `DayType` 이 그렇고, 혼용 금지는 값이 아니라 조합 규칙이라 타입으로 막히지 않는다.
 */
export type ProductExposureDayType =
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

// ===== 메뉴판(메뉴그룹 + 메뉴) =====

export interface ProductCategoryResponse {
  id: number;
  name: string;
  description: string | null;
  sort: number;
}

export interface ProductCategoryCreateRequest {
  shopId: number;
  name: string;
  description?: string;
}

export interface ProductCategoryUpdateRequest extends ProductCategoryCreateRequest {}

// ===== 메뉴 CRUD =====

/** 등록·수정 공통 본문. 수정은 여기에 경로 `{id}` 가 더해진다(§2-2) */
export interface ProductCreateRequest {
  shopId: number;
  productCategoryId?: number | null;
  name: string;
  composition?: string;
  description?: string;
  originalPrice: number;
  discountPrice?: number | null;
  singleServing?: boolean;
  spiciness?: number | null;
  representative?: boolean;
  ratingExcluded?: boolean;
}

export interface ProductUpdateRequest extends ProductCreateRequest {}

export interface ProductDeleteRequest {
  shopId: number;
  productIds: number[];
}

// ===== 순서 변경 (§4) — sort 를 계산해 보내지 않고 순서 있는 id 배열만 보낸다 =====

export interface ProductCategoryOrderRequest {
  shopId: number;
  productCategoryIds: number[];
}

export interface ProductOrderRequest {
  shopId: number;
  /** 미분류 목록도 재정렬 대상이라 nullable */
  productCategoryId: number | null;
  productIds: number[];
}

export interface ProductCategoryMoveRequest {
  shopId: number;
  targetProductCategoryId: number | null;
  productIds: number[];
  /** 도착 그룹의 최종 순서. 빠뜨리면 서버가 맨 끝에 append 해 놓은 위치가 무시된다 */
  targetOrderedProductIds: number[];
}

// ===== 메뉴 상세 =====

export interface ProductDetailResponse {
  id: number;
  shopId: number;
  productCategoryId: number | null;
  productCategoryName: string | null;
  name: string;
  composition: string | null;
  description: string | null;
  originalPrice: number;
  discountPrice: number | null;
  singleServing: boolean;
  spiciness: number | null;
  representative: boolean;
  ratingExcluded: boolean;
  soldOut: boolean;
  visible: boolean;
  imageUrl: string | null;
  vegetarianType: VegetarianType | null;
  /** 노출기간(요일·시간대 또는 기간)이 설정되어 있는지 여부. 상세값은 노출기간 조회 API(§6)가 담당 */
  exposureScheduled: boolean;
}

// ===== 옵션그룹 · 옵션 (§5) =====

export interface ProductOptionResponse {
  id: number;
  name: string;
  additionalPrice: number;
  sort: number;
}

export interface ProductOptionGroupResponse {
  id: number;
  name: string;
  description: string | null;
  required: boolean;
  multipleSelect: boolean;
  minSelect: number | null;
  maxSelect: number | null;
  sort: number;
  /** 이 그룹이 연결된 메뉴 수. 해제 영향 안내와 마지막 연결 차단에 쓴다 */
  linkedProductCount: number;
  options: ProductOptionResponse[];
}

export interface ProductOptionGroupSaveRequest {
  shopId: number;
  /** 등록 시에만 필수 — 이 메뉴에 곧바로 연결된다. 링크 0건 그룹은 어디서도 보이지 않는 고아가 된다 */
  productId: number;
  name: string;
  description?: string;
  required: boolean;
  multipleSelect: boolean;
  minSelect?: number | null;
  maxSelect?: number | null;
}

export interface ProductOptionSaveRequest {
  shopId: number;
  name: string;
  additionalPrice: number;
}

export interface ProductOptionGroupSortRequest {
  shopId: number;
  optionGroupIds: number[];
}

export interface ProductOptionSortRequest {
  shopId: number;
  optionIds: number[];
}

/** 옵션그룹 해제 전 영향 확인(§5-2) */
export interface ProductOptionGroupLinkedProductResponse {
  id: number;
  name: string;
}

/** 가게 옵션그룹 전체의 연결 메뉴 벌크 조회(§5-2) — 그룹마다 개별 조회하는 N+1을 피하기 위한 응답 */
export interface ProductOptionGroupLinkedProductsResponse {
  optionGroupId: number;
  products: ProductOptionGroupLinkedProductResponse[];
}

// ===== 노출기간 (§6) =====

export interface ProductExposureHourResponse {
  dayType: ProductExposureDayType;
  /** `HH:mm` 또는 `HH:mm:ss`. 비어 있으면 종일 */
  startTime: string | null;
  endTime: string | null;
}

export interface ProductExposureResponse {
  startDate: string | null;
  endDate: string | null;
  hours: ProductExposureHourResponse[];
  exposedNow: boolean;
  hiddenReason: ProductHiddenReason | null;
}

export interface ProductExposureRequest {
  shopId: number;
  startDate?: string | null;
  endDate?: string | null;
  hours: ProductExposureHourResponse[];
}

// ===== 이미지 (§7-1) =====

export interface ProductImageResponse {
  id: number;
  imageUrl: string;
  sort: number;
}

export interface ProductImageChangeRequestResponse {
  id: number;
  status: ApprovalStatus;
  imageUrl: string | null;
  rejectReason: string | null;
  requestedAt: string;
}

export interface ProductImageListResponse {
  images: ProductImageResponse[];
  /** 요청 이력. 검수 대기·반려 건도 여기에 담겨 내려온다 */
  requests: ProductImageChangeRequestResponse[];
}

export interface ProductImageSortRequest {
  shopId: number;
  imageIds: number[];
}

// ===== 채식 (§7-1) =====

export interface ProductVegetarianRequestItemResponse {
  id: number;
  vegetarianType: VegetarianType;
  ingredients: string | null;
  description: string | null;
  status: ApprovalStatus;
  rejectReason: string | null;
}

export interface ProductVegetarianResponse {
  /** 승인되어 실제 반영된 값. 요청 중인 값과 다를 수 있다 */
  vegetarianType: VegetarianType | null;
  /** 요청 이력. 검수 대기·반려 건도 여기에 담겨 내려온다 */
  requests: ProductVegetarianRequestItemResponse[];
  /** 가게 카테고리가 채식 불가면 false — 서버가 판정해 내려준다 */
  changeable: boolean;
}

export interface ProductVegetarianRequestBody {
  shopId: number;
  vegetarianType: VegetarianType;
  ingredients: string;
  description?: string;
}
