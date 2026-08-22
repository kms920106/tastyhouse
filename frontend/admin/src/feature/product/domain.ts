// 상품 도메인 모델 — UI 와 api/product.service 가 공유한다.

import type { ApprovalStatusValue, OptionGroupType, VegetarianTypeValue } from "@/api/product/product.dto";

// api/product 계층에서 정의한 enum 문자열 유니온을 도메인에서도 그대로 쓴다.
export type { OptionGroupType };

export interface ProductListItem {
  id: number;
  shopName: string;
  name: string;
  originalPrice: number;
  discountPrice: number | null;
  discountRate: number | null;
  representative: boolean;
  soldOut: boolean;
  visible: boolean;
  sort: number;
}

export interface ProductDetail {
  id: number;
  shopId: number;
  productCategoryId: number | null;
  name: string;
  description: string | null;
  originalPrice: number;
  discountPrice: number | null;
  discountRate: number | null;
  rating: number | null;
  reviewCount: number | null;
  representative: boolean;
  spiciness: number | null;
  soldOut: boolean;
  visible: boolean;
  sort: number;
  createdAt: string;
  updatedAt: string;
}

export interface Option {
  id: number;
  name: string;
  additionalPrice: number;
  soldOut: boolean;
  /** 일회용컵 제공 개수(1~10) — CUP_DEPOSIT 그룹의 보증금 옵션에만 존재. 그 외에는 null */
  cupCount: number | null;
  /** 보증금액(cupCount × 정책 요율) — 서버가 계산해 내려준다. 보증금 옵션이 아니면 null */
  depositAmount: number | null;
  /** 개인컵 사용 시 할인 금액 — 개인컵 옵션에만 존재. 그 외에는 null */
  personalCupDiscountAmount: number | null;
}

export interface OptionGroup {
  id: number;
  name: string;
  description: string | null;
  required: boolean;
  multipleSelect: boolean;
  minSelect: number;
  maxSelect: number;
  common: boolean;
  groupType: OptionGroupType;
  options: Option[];
}

export interface ProductOptionGroups {
  optionGroups: OptionGroup[];
}

export interface ProductCategory {
  id: number;
  shopId: number;
  name: string;
  sort: number;
  visible: boolean;
}

// ===== 메뉴 검수 (이미지 변경 요청 · 채식 설정 요청) =====

// api/product 계층에서 정의한 enum 문자열 유니온을 도메인에서도 그대로 쓴다.
export type ApprovalStatus = ApprovalStatusValue;
export type VegetarianType = VegetarianTypeValue;

export interface ProductImageChangeRequestItem {
  id: number;
  productId: number;
  shopId: number;
  productName: string;
  /** 검수 대상 이미지 URL. 파일이 없으면 null — 이미지를 못 보면 승인 판단이 불가하다 */
  imageUrl: string | null;
  status: ApprovalStatus;
  rejectReason: string | null;
}

export interface ProductVegetarianRequestItem {
  id: number;
  productId: number;
  shopId: number;
  productName: string;
  vegetarianType: VegetarianType;
  /** 채소 외 포함 재료 — 채식 승인의 유일한 근거이므로 목록에서 잘라내지 않는다 */
  ingredients: string;
  description: string | null;
  status: ApprovalStatus;
  rejectReason: string | null;
}

// ===== 사장님 추천(대표 메뉴) 검수 =====

/**
 * 사장님 추천 지정 요청 한 건.
 *
 * 검수 기준("메뉴명과 메뉴 이미지가 일치")을 사람이 판단해야 하므로 메뉴명과 이미지를 함께 보관한다.
 */
export interface ProductRepresentativeRequestItem {
  id: number;
  productId: number;
  shopId: number;
  shopName: string;
  productName: string;
  /** 메뉴 대표 이미지 URL. 없으면 null — 이미지 없는 메뉴는 추천 지정 대상이 아니므로 반려 근거가 된다 */
  imageUrl: string | null;
  status: ApprovalStatus;
  rejectReason: string | null;
}
