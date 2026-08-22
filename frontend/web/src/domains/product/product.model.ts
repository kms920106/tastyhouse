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

/**
 * 메뉴 영양성분·알레르기.
 *
 * DTO 와 필드가 1:1 대응하지만, `src/app/**` 이 DTO 타입을 Props 로 쓸 수 없다는 규칙에 따라
 * 모델을 따로 둔다(`src/domains/CLAUDE.md` §8.8).
 */
export interface ProductNutrition {
  servingSize: string | null
  totalAmount: string | null
  flavor: string | null
  size: string | null
  /** 열량 (kcal) */
  calorie: number | null
  /** 당류 (g) */
  sugars: number | null
  /** 단백질 (g) */
  protein: number | null
  /** 포화지방 (g) */
  saturatedFat: number | null
  /** 나트륨 (mg) */
  natrium: number | null
  carbohydrate: number | null
  cholesterol: number | null
  fat: number | null
  transFat: number | null
  caffeine: number | null
  setMenu: boolean
  /** 서버가 한글 라벨로 내려주므로 화면이 코드→라벨 매핑을 갖지 않는다 */
  allergens: string[]
}
