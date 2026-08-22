export interface ProductCategory {
  categoryName: string
  products: Product[]
}

export interface Product {
  id: number
  imageUrl: string
  spiciness: number | null
  name: string
  originalPrice: number
  discountPrice: number
  discountRate: number | null
  rating: number | null
  reviewCount: number | null
  representative: boolean | null
}

/** 옵션그룹 유형. `CUP_DEPOSIT`은 일회용컵 보증금 옵션그룹이며 일반 옵션그룹과 별도 섹션으로 렌더링한다 */
export type ProductOptionGroupType = 'NORMAL' | 'CUP_DEPOSIT'

export interface ProductOptionGroup {
  id: number
  name: string
  description: string | null
  required: boolean
  multipleSelect: boolean
  minSelect: number
  maxSelect: number
  common: boolean
  groupType: ProductOptionGroupType
  options: ProductOption[]
}

export interface ProductOption {
  id: number
  name: string
  additionalPrice: number
  soldOut: boolean
  /** 보증금 부과 대상 음료 개수(1~10). 보증금 옵션그룹의 옵션만 값을 가짐 */
  cupCount: number | null
  /** 보증금 금액(= cupCount * 정책 요율). 서버가 계산한 값이며 표시 전용이다 */
  depositAmount: number | null
  /** 개인컵 사용 할인 금액. 개인컵 옵션이 아니면 null. 보증금이 아니라 상품 할인 축이다 */
  personalCupDiscountAmount: number | null
}
