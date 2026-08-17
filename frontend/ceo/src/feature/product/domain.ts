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
