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
  /** 중량 표기(치킨 등 법정 의무표시). 최대 50자 */
  weightText?: string;
  originalPrice: number;
  discountPrice?: number | null;
  singleServing?: boolean;
  spiciness?: number | null;
  representative?: boolean;
  ratingExcluded?: boolean;
  /**
   * 다중 가게 연결 (P2-5 【변경】).
   *
   * 생략하면 서버가 `shopId`·`productCategoryId` 로 **단일 연결**을 만든다 — 가게가 하나인
   * 점주의 기존 등록 흐름이 그대로 도는 것이 이 설계의 안전장치다.
   */
  links?: { shopId: number; productCategoryId: number }[];
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
  /** 중량 표기(치킨 등 법정 의무표시). 미입력이면 null */
  weightText: string | null;
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
  /** 품절 여부. 조작은 품절·숨김 화면이 하고 여기서는 표시만 한다 */
  soldOut: boolean;
  /**
   * 노출 여부.
   *
   * **이 목록은 감춘(삭제한) 옵션도 포함한다** — 서버가 소프트 삭제를 쓰기 때문이다. 화면이 이
   * 값으로 걸러내지 않으면 삭제한 옵션이 살아 있는 것처럼 보인다.
   */
  visible: boolean;
  /** 일회용컵 제공 개수. 보증금 옵션에만 채워진다(`backend.md` §3-7-1) */
  cupCount: number | null;
  /** `cupCount × 정책 요율`을 서버가 계산해 내려준 값. 금액의 진실원은 이쪽이다 */
  depositAmount: number | null;
  /** 개인컵 사용 옵션의 할인 금액. 보증금이 아니라 상품 할인 축이다 */
  personalCupDiscountAmount: number | null;
}

export interface ProductOptionGroupResponse {
  id: number;
  name: string;
  /** 옵션그룹 유형. 서버가 항상 채워 내려준다(레거시 행은 `NORMAL`) */
  groupType: ProductOptionGroupType;
  /** 노출 여부. 옵션과 같은 이유로 감춘 그룹도 목록에 포함되므로 화면이 걸러낸다 */
  visible: boolean;
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
  /**
   * 등록(POST)에서만 필수 — 이 메뉴에 곧바로 연결된다. 링크 0건 그룹은 어디서도 보이지 않는
   * 고아가 된다. 수정(PUT)은 서버가 받지 않으므로 생략한다.
   */
  productId?: number;
  name: string;
  description?: string;
  required: boolean;
  multipleSelect: boolean;
  minSelect?: number | null;
  maxSelect?: number | null;
  /**
   * 옵션그룹 유형. **등록(POST)에서만 보낸다** — 수정(PUT)은 서버가 받지 않는다.
   * 유형 전환을 허용하면 과거 주문 스냅샷의 해석이 바뀌므로 경로 자체가 없다(`backend.md` §3-6).
   */
  groupType?: ProductOptionGroupType;
}

export interface ProductOptionSaveRequest {
  shopId: number;
  name: string;
  additionalPrice: number;
  /** 보증금 옵션에만 보낸다(`1~10`). 일반 옵션에 보내면 서버가 거부한다 */
  cupCount?: number | null;
  /** 개인컵 사용 옵션에만 보낸다(`0` 이상). 보증금 그룹 밖에서는 서버가 거부한다 */
  personalCupDiscountAmount?: number | null;
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

// =====================================================================================
// 옵션그룹 합치기 DTO (`docs/tasks/backend.md` §2-6)
//
// 신규 컨트롤러 `ProductOptionGroupMergeApiController` 의 4개 엔드포인트에 대응한다.
// =====================================================================================

/** 합치기 진입 경로. 서버가 이력(`PRODUCT_OPTION_GROUP_MERGE_HISTORY`)에 남긴다 */
export type ProductOptionGroupMergeEntryType = "RECOMMENDED" | "MANUAL";

/**
 * 옵션 단위 diff 갈래.
 *
 * `ONLY_IN_CANDIDATE` 가 가장 중요하다 — 흡수될 그룹에만 있는 옵션은 재부모화되지 않고
 * 함께 숨겨지므로(§2-3) "합치면 사라짐"을 사용자에게 반드시 알려야 한다.
 */
export type ProductOptionGroupMergeDiffType = "SAME" | "ONLY_IN_BASE" | "ONLY_IN_CANDIDATE" | "PRICE_DIFFERS";

/** 추천 묶음의 공통 옵션 대표 1세트 */
export interface ProductOptionGroupMergeSuggestionOptionResponse {
  id: number;
  name: string;
  additionalPrice: number;
}

export interface ProductOptionGroupMergeSuggestionGroupResponse {
  id: number;
  linkedProductCount: number;
  linkedProductNames: string[];
}

export interface ProductOptionGroupMergeSuggestionResponse {
  /**
   * 제외([X]) 요청에 그대로 실어 보내는 **불투명 토큰**.
   *
   * 클라이언트가 구조를 해석하거나 재계산하지 않는다 — 서버가 `optionGroupIds` 로 서명을
   * 재계산해 위조·낡은 토큰을 `PRODUCT_OPTION_GROUP_MERGE_SIGNATURE_MISMATCH` 로 거부한다.
   */
  signature: string;
  name: string;
  minSelect: number | null;
  maxSelect: number | null;
  groupCount: number;
  linkedProductCount: number;
  options: ProductOptionGroupMergeSuggestionOptionResponse[];
  groups: ProductOptionGroupMergeSuggestionGroupResponse[];
}

export interface ProductOptionGroupMergeExclusionRequest {
  shopId: number;
  signature: string;
  optionGroupIds: number[];
}

export interface ProductOptionGroupMergePreviewOptionResponse {
  id: number;
  name: string;
  additionalPrice: number;
  soldOut: boolean;
  visible: boolean;
  diffType: ProductOptionGroupMergeDiffType;
}

/** 기준·후보 공통 항목. `*Differs` 는 기준과 다른 필드를 화면이 강조하기 위한 서버 판정값 */
export interface ProductOptionGroupMergePreviewItemResponse {
  id: number;
  name: string;
  description: string | null;
  required: boolean;
  multipleSelect: boolean;
  minSelect: number | null;
  maxSelect: number | null;
  linkedProductNames: string[];
  nameDiffers: boolean;
  minSelectDiffers: boolean;
  maxSelectDiffers: boolean;
  options: ProductOptionGroupMergePreviewOptionResponse[];
}

export interface ProductOptionGroupMergePreviewResponse {
  base: ProductOptionGroupMergePreviewItemResponse;
  /** 기준을 제외한 후보들 */
  candidates: ProductOptionGroupMergePreviewItemResponse[];
  mergeable: boolean;
  /** `mergeable=false` 일 때만 채워지는 사유 `ErrorCode` */
  blockedReason: string | null;
}

/** 조회 파라미터. `optionGroupIds` 는 반복 query 파라미터로 직렬화한다 */
export interface ProductOptionGroupMergePreviewParams {
  shopId: number;
  baseOptionGroupId: number;
  optionGroupIds: number[];
}

export interface ProductOptionGroupMergeRequest {
  shopId: number;
  /** 흡수 대상. 기준(base)은 경로 `{id}` 로 가므로 여기 포함되면 서버가 거부한다 */
  optionGroupIds: number[];
  entryType: ProductOptionGroupMergeEntryType;
}

// =====================================================================================
// 일회용컵 보증금 DTO (`docs/tasks/backend.md` §3)
// =====================================================================================

/**
 * 옵션그룹 유형.
 *
 * **`ProductOptionType`(NORMAL/COMMON)과 다른 축이다.** 그것은 일반/공통 옵션 테이블 갈래를
 * 가리키는 요청 전용 값이고, 이쪽은 DB 에 저장되는 옵션그룹의 규제 유형이다. 이름이 비슷해
 * 혼동이 실제로 생기므로 두 타입을 섞어 쓰지 않는다.
 */
export type ProductOptionGroupType = "NORMAL" | "CUP_DEPOSIT";

// ===== 사장님 추천 (대표 메뉴) =====

/**
 * 대표 메뉴 지정 요청.
 *
 * 응답이 요청한 `productIds` 보다 **짧을 수 있다** — 이미 대표거나 검수 대기 중인 메뉴는
 * 서버가 400 을 내지 않고 조용히 건너뛰기 때문이다.
 */
export interface ProductRepresentativeRequestBody {
  shopId: number;
  productIds: number[];
}

// ===== 영양성분·알레르기 (법정 표시 의무) =====
// 검수 대상이 아니다 — 점주만이 아는 사실 정보라 관리자가 검증할 근거가 없고,
// 정확성 책임은 가맹본사·가게에 있다.

/**
 * 알레르기 유발성분 코드.
 *
 * 화면은 이 유니온으로 목록을 만들지 않는다 — 체크박스 목록은 서버(`GET .../allergens`)가
 * 공급하므로 항목이 늘어도 화면 배포가 필요 없다. 이 타입은 저장 값의 형태만 고정한다.
 */
export type AllergenCode = string;

/** 알레르기 체크박스 한 칸. 서버가 코드와 한글 라벨을 함께 준다 */
export interface AllergenOptionResponse {
  code: AllergenCode;
  label: string;
}

/**
 * 영양성분·알레르기.
 *
 * 필수 5종(`calorie`·`sugars`·`protein`·`saturatedFat`·`natrium`)은 전부 채우거나 전부 비운다 —
 * 일부만 채운 표시는 법적으로 의미가 없고 오히려 위반이다. 판정은 서버와 폼이 함께 한다.
 */
export interface ProductNutritionResponse {
  /** 1회 제공량 (예: `100g`) */
  servingSize: string | null;
  /** 총 제공량 */
  totalAmount: string | null;
  flavor: string | null;
  size: string | null;
  /** 열량 (kcal, 필수 5종) */
  calorie: number | null;
  /** 당류 (g, 필수 5종) */
  sugars: number | null;
  /** 단백질 (g, 필수 5종) */
  protein: number | null;
  /** 포화지방 (g, 필수 5종) */
  saturatedFat: number | null;
  /** 나트륨 (mg, 필수 5종) */
  natrium: number | null;
  carbohydrate: number | null;
  cholesterol: number | null;
  fat: number | null;
  transFat: number | null;
  caffeine: number | null;
  /** 세트 메뉴 여부. true 면 손님 화면에 조합 안내문구가 함께 노출된다 */
  setMenu: boolean;
  /** 점주 조회는 코드 배열이다(손님 조회는 한글 라벨 배열) */
  allergens: AllergenCode[];
}

/** 전체 교체(PUT) — 메뉴당 1건이고 부분 수정 개념이 없다 */
export interface ProductNutritionUpdateRequest {
  shopId: number;
  servingSize?: string;
  totalAmount?: string;
  flavor?: string;
  size?: string;
  calorie?: number | null;
  sugars?: number | null;
  protein?: number | null;
  saturatedFat?: number | null;
  natrium?: number | null;
  carbohydrate?: number | null;
  cholesterol?: number | null;
  fat?: number | null;
  transFat?: number | null;
  caffeine?: number | null;
  setMenu: boolean;
  allergens: AllergenCode[];
}

// ===== 가격 체계 확장 (가격명 + 채널별 가격) =====

/** 가격 행 한 줄. `storePrice`·`pickupPrice` 는 매장가격 인증 전이면 항상 null 이다 */
export interface ProductPriceResponse {
  id: number;
  priceName: string | null;
  deliveryPrice: number;
  storePrice: number | null;
  pickupPrice: number | null;
  sort: number;
}

/**
 * 가격 전체 교체(PUT).
 *
 * 순서 변경 API 와 같은 의미론이다 — 보내지 않은 행은 삭제된다. 첫 행(`sort=0`)의 `deliveryPrice`
 * 를 서버가 `PRODUCT.original_price` 에 동기화하므로, 기존 가격 하나만 쓰는 화면들이 그대로 돈다.
 */
export interface ProductPriceUpdateRequest {
  shopId: number;
  prices: {
    /** 기존 행이면 그 id, 새로 추가한 행이면 생략 */
    id?: number;
    priceName?: string;
    deliveryPrice: number;
    storePrice?: number | null;
    pickupPrice?: number | null;
    sort: number;
  }[];
}

// ===== 메뉴 정보에 대한 고객 의견 (점주 확인) =====

/** 의견 유형 코드. 손님 화면의 라디오 항목과 같은 집합이다 */
export type ProductFeedbackType = "PRICE" | "IMAGE" | "COMPOSITION" | "SOLD_OUT" | "ETC";

/**
 * 의견 한 줄. **제보자 정보(회원 id·닉네임)는 내려오지 않는다** —
 * 점주가 특정 손님을 식별하면 보복 우려가 있고, 제보의 목적은 정보 수정이지 손님 응대가 아니다.
 */
export interface ProductFeedbackResponse {
  productId: number;
  productName: string;
  feedbackType: ProductFeedbackType;
  /** 지난 한 주 동안 같은 유형으로 접수된 건수 */
  count: number;
  /** `ETC` 유형의 서술 내용 (최대 10건) */
  contents: string[];
}

/** 아이콘 빨간 점 표시용 */
export interface ProductFeedbackUnreadResponse {
  hasUnread: boolean;
}

// ===== 메뉴-가게 연결 (N:M) =====

/**
 * 연결 후보 한 줄.
 *
 * **점주가 소유한 전체 가게**가 내려오고 `linked` 로 연결 여부를 구분한다 —
 * 화면이 토글로 켜고 끌 수 있게 하려는 것이다.
 */
export interface ProductShopLinkResponse {
  shopId: number;
  shopName: string;
  /** 연결돼 있지 않으면 null */
  productCategoryId: number | null;
  productCategoryName: string | null;
  linked: boolean;
}

/**
 * 연결 전체 교체(PUT).
 *
 * 목록에 없는 가게는 연결이 해제된다. 최상위 `shopId` 는 **권한 판정 기준 가게**이고
 * `links[].shopId` 가 실제 연결 대상이다 — 둘을 혼동하면 남의 가게를 건드리게 된다.
 */
export interface ProductShopLinkUpdateRequest {
  shopId: number;
  links: { shopId: number; productCategoryId: number }[];
}

/** 메뉴 불러오기(가게 기준). 대상 가게는 경로에 있고 메뉴그룹만 본문으로 보낸다 */
export interface ProductShopLinkCreateRequest {
  productCategoryId: number;
}
