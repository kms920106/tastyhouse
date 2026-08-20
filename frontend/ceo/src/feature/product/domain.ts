// 점주 품절·숨김 도메인 모델 — UI 가 import 하는 유일한 타입.
// DTO(`*.dto.ts`)는 이 경계를 넘지 않는다. 이번 응답은 필드가 그대로 대응해
// DTO → domain 변환이 없으므로 `product.service.ts` 를 두지 않는다.

export type { ProductOptionType, ProductReleaseTarget } from "@/api/product/product.dto";

import type { ProductOptionType } from "@/api/product/product.dto";

export interface AvailabilityMenuRow {
  id: number;
  name: string;
  originalPrice: number;
  discountPrice: number | null;
  imageUrl: string | null;
  soldOut: boolean;
  /** ISO-8601. null 이면 무기한 품절 또는 판매중 */
  soldOutUntil: string | null;
  visible: boolean;
  /** 사장님 추천 메뉴 */
  representative: boolean;
  sort: number;
}

export interface AvailabilityMenuGroup {
  /** 카테고리 미지정 메뉴는 null */
  categoryId: number | null;
  categoryName: string | null;
  sort: number;
  products: AvailabilityMenuRow[];
}

export interface AvailabilityOptionRow {
  id: number;
  optionType: ProductOptionType;
  name: string;
  additionalPrice: number;
  soldOut: boolean;
  soldOutUntil: string | null;
  visible: boolean;
  sort: number;
}

export interface AvailabilityOptionGroup {
  optionGroupId: number;
  optionType: ProductOptionType;
  name: string;
  required: boolean;
  minSelect: number | null;
  maxSelect: number | null;
  linkedProductNames: string[];
  sort: number;
  options: AvailabilityOptionRow[];
}

/** 일괄 처리 결과. 부분실패는 `failed` 에 담겨 HTTP 200 으로 돌아온다 */
export interface AvailabilityChangeOutcome {
  succeededIds: number[];
  failed: { id: number; name: string; errorCode: string; message: string }[];
}

/** 선택 대상. 옵션은 id 만으로 갈래를 알 수 없어 `optionType` 을 함께 들고 다닌다 */
export interface OptionSelection {
  optionId: number;
  optionType: ProductOptionType;
}

/** 화면 탭. 메뉴/옵션은 조회 엔드포인트가 달라 URL 파라미터로 관리한다 */
export type AvailabilityTab = "menu" | "option";

// =====================================================================================
// 점주 메뉴·옵션 관리 도메인 모델 (`docs/tasks/frontend.md` §3~§5)
//
// 위 품절·숨김과 마찬가지로 DTO 필드가 1:1 대응해 변환 계층(`product.service.ts`)을 두지 않고
// `re-export` 로 이름만 도메인 쪽 명칭에 맞춘다.
// =====================================================================================

export type {
  ApprovalStatus,
  ProductExposureDayType,
  ProductHiddenReason,
  VegetarianType,
} from "@/api/product/product.dto";

import type {
  ApprovalStatus,
  ProductExposureDayType,
  ProductHiddenReason,
  VegetarianType,
} from "@/api/product/product.dto";

/** 메뉴그룹(카테고리) */
export interface MenuCategory {
  id: number;
  name: string;
  description: string | null;
  sort: number;
}

/** 메뉴판 목록의 메뉴 한 줄. 품절·숨김 화면의 행과 필드가 같아 그 타입을 그대로 쓴다 */
export type MenuBoardRow = AvailabilityMenuRow;

/** 메뉴판 목록의 그룹. 드래그 정렬 단위 */
export type MenuBoardGroup = AvailabilityMenuGroup;

export interface MenuDetail {
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
  /** 노출기간(요일·시간대 또는 기간)이 설정되어 있는지 여부. 상세값은 `MenuExposure` 조회로 얻는다 */
  exposureScheduled: boolean;
}

// ===== 옵션그룹 · 옵션 =====

export interface MenuOption {
  id: number;
  name: string;
  additionalPrice: number;
  sort: number;
}

export interface MenuOptionGroup {
  id: number;
  name: string;
  description: string | null;
  required: boolean;
  multipleSelect: boolean;
  minSelect: number | null;
  maxSelect: number | null;
  sort: number;
  /** 연결된 메뉴 수. 0 이 될 수 없다(마지막 연결 해제는 서버가 막는다) */
  linkedProductCount: number;
  options: MenuOption[];
}

export interface LinkedProductSummary {
  id: number;
  name: string;
}

// ===== 노출기간 =====

export interface MenuExposureHour {
  dayType: ProductExposureDayType;
  /** 비어 있으면 종일 */
  startTime: string | null;
  endTime: string | null;
}

export interface MenuExposure {
  startDate: string | null;
  endDate: string | null;
  hours: MenuExposureHour[];
  exposedNow: boolean;
  hiddenReason: ProductHiddenReason | null;
}

/**
 * 요일 선택 방식.
 *
 * 묶음(`DAILY`/`WEEKDAY`/`WEEKEND`/`HOLIDAY`)과 개별 요일은 **혼용할 수 없다**
 * (`PRODUCT_EXPOSURE_DAY_TYPE_MIXED`). UI 가 라디오로 방식을 먼저 고르게 해
 * 애초에 섞을 수 없게 만들고 서버 에러에 의존하지 않는다.
 */
export type ExposureDaySelectionMode = "PRESET" | "INDIVIDUAL";

// ===== 이미지 · 채식 (승인 워크플로) =====

export interface MenuImage {
  id: number;
  imageUrl: string;
  sort: number;
}

export interface MenuImageChangeRequest {
  id: number;
  status: ApprovalStatus;
  imageUrl: string | null;
  rejectReason: string | null;
  requestedAt: string;
}

export interface MenuImageList {
  images: MenuImage[];
  /** 요청 이력 전체 */
  requests: MenuImageChangeRequest[];
  /** 검수 대기·반려 중인 요청. 없으면 null — `MenuVegetarian.pendingRequest` 와 같은 규약 */
  pendingRequest: MenuImageChangeRequest | null;
}

export interface MenuVegetarianRequest {
  id: number;
  vegetarianType: VegetarianType;
  ingredients: string | null;
  description: string | null;
  status: ApprovalStatus;
  rejectReason: string | null;
}

export interface MenuVegetarian {
  vegetarianType: VegetarianType | null;
  /** 요청 이력 전체 */
  requests: MenuVegetarianRequest[];
  /** 검수 대기·반려 중인 요청. 없으면 null — `MenuImageList.pendingRequest` 와 같은 규약 */
  pendingRequest: MenuVegetarianRequest | null;
  /** 가게 카테고리가 채식 불가면 false — 서버 판정을 그대로 쓴다 */
  changeable: boolean;
}
