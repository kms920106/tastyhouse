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
