// 상품 도메인 모델 — UI 와 api/product.service 가 공유한다.

import type { OptionGroupType } from "@/api/product/product.dto";

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
