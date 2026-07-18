// 상품 도메인 모델 — UI 와 api/product.service 가 공유한다.

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
