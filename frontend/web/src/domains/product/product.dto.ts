import type { PaginationParams } from '@/types/common'
import type { OrderMethodType } from '../order'
import type { ProductOptionGroup } from './product.model'

export interface ProductReviewListQuery extends PaginationParams {
  hasImage?: boolean
}

export interface ProductListItemResponse {
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

/**
 * 가격 한 행 — 손님 화면용.
 *
 * 점주 화면(ceo)의 가격 행과 달리 **채널별 가격이 아니라 이미 해석된 단일 가격**(`price`)만 담는다.
 * 주문유형(`orderMethod`)에 따라 배달가/픽업가 중 무엇을 쓸지는 서버가 단독으로 정하며, 손님 화면이
 * 그 판단을 대신하면 서버 계산과 어긋나 주문이 전부 거절된다(`ORDER_PRODUCT_AMOUNT_MISMATCH`).
 */
export interface ProductPriceResponse {
  /**
   * 가격 행 id. 서버 응답 필드명이 `priceId` 이므로 `id` 로 줄여 쓰지 않는다 —
   * 이름이 어긋나면 값이 `undefined` 가 되어 선택 상태가 어디에도 붙지 않고,
   * "담기 버튼이 조용히 잠긴 채 아무 일도 일어나지 않는" 증상이 된다.
   */
  priceId: number
  /** null 이면 단일 가격 — 화면에 하위 항목으로 표시하지 않는다 */
  priceName: string | null
  /** 주문유형에 따라 서버가 해석한 최종 가격. 화면은 이 값을 그대로 쓴다 */
  price: number
}

export interface ProductDetailResponse {
  id: number
  name: string
  description: string
  /** 중량 표기(치킨 등 법정 의무표시). 미입력이면 null */
  weightText: string | null
  originalPrice: number
  discountPrice: number | null
  discountRate: number | null
  soldOut: boolean
  shopId: number
  /**
   * 그 상품의 노출 메뉴 평가 수.
   *
   * 상품 평점(`rating`)은 기존 필드를 그대로 쓴다 — 값의 근거만 MENU_REVIEW로 바뀌었고
   * API 계약(필드명·타입)은 동일하다.
   */
  menuReviewCount: number
  /**
   * 가격 행 목록.
   *
   * 행이 1개면 기존 `originalPrice`·`discountPrice` 와 같은 값이라 화면 동작이 그대로다.
   * 2개 이상이면 가격명을 함께 보여주고, 주문 전 손님이 하나를 골라야 한다.
   */
  prices: ProductPriceResponse[]
}

export interface ProductBatchItemRequest {
  productId: number
  optionId: number | null
}

export interface ProductBatchRequest {
  items: ProductBatchItemRequest[]
  /**
   * 가격 행(`prices`)의 가격을 해석할 주문유형. 미지정이면 서버가 `DELIVERY` 로 본다.
   *
   * 장바구니·주문서는 자기 주문유형을 알고 있으므로 항상 보낸다 — 보내지 않으면 포장 주문에서도
   * 배달가로 표시돼 주문 접수 시 서버 계산과 어긋난다(`ORDER_PRODUCT_AMOUNT_MISMATCH`).
   */
  orderMethod?: OrderMethodType
}

export interface ProductBatchOptionResponse {
  id: number
  name: string
  price: number
  /** 보증금 부과 대상 음료 개수. 보증금 옵션이 아니면 null */
  cupCount: number | null
  /** 보증금 금액. 보증금 옵션이 아니면 null */
  depositAmount: number | null
  /** 개인컵 사용 할인 금액. 개인컵 옵션이 아니면 null */
  personalCupDiscountAmount: number | null
}

export interface ProductBatchItemResponse {
  id: number
  available: boolean
  name: string | null
  imageUrl: string | null
  originalPrice: number | null
  discountPrice: number | null
  options: ProductBatchOptionResponse[]
  /**
   * 가격 행 목록.
   *
   * 장바구니는 담을 때 고른 `priceId` 만 보관하므로, 그 값으로 가격명·가격을 되찾는 데 쓴다.
   * `price` 는 요청한 `orderMethod` 로 서버가 이미 해석한 값이라 화면이 다시 계산하지 않는다.
   * 가격 행이 없는 메뉴(이관 이전 데이터)와 `available=false` 면 빈 배열이다.
   */
  prices: ProductPriceResponse[]
}

export interface ProductBatchResponse {
  products: ProductBatchItemResponse[]
}

export interface ProductReviewCountResponse {
  reviewCount: number
}

export interface ProductImagesResponse {
  imageUrls: string[]
}

export interface ProductOptionsResponse {
  optionGroups: ProductOptionGroup[]
}

interface ProductReviewListItemResponse {
  id: number
  imageUrls: string[]
  totalRating: number
  content: string
  memberId: number
  memberNickname: string
  memberProfileImageUrl: string | null
  createdAt: string
  productId: number
  productName: string
  ownerReplyContent: string | null
  ownerReplyCreatedAt: string | null
}

export interface ProductReviewStatisticsResponse {
  totalRating: number | null
  totalReviewCount: number
  averageTasteRating: number
  averageAmountRating: number
  averagePriceRating: number
}

export interface ProductReviewsByRatingResponse {
  reviewsByRating: Record<string, ProductReviewListItemResponse[]>
  allReviews: ProductReviewListItemResponse[]
  totalReviewCount: number
}

export interface ProductTodayDiscountListItemResponse {
  id: number
  name: string
  shopName: string
  imageUrl: string
  originalPrice: number
  discountPrice: number
  discountRate: number
}

/**
 * 알레르기는 코드가 아니라 **한글 라벨 배열**로 내려온다 — 손님 화면이 코드→라벨 매핑표를
 * 들고 있지 않게 하려는 것이다. 미입력 메뉴는 응답 `data` 가 null 이다.
 */
export interface ProductNutritionResponse {
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
  /** true 면 수치 위에 "메뉴구성에 따라 다르다"는 안내문구를 먼저 보여준다 */
  setMenu: boolean
  /** 한글 라벨 배열 (예: `["우유","땅콩"]`) */
  allergens: string[]
}
