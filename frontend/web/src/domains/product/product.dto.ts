import type { PaginationParams } from '@/types/common'
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

export interface ProductDetailResponse {
  id: number
  name: string
  description: string
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
}

export interface ProductBatchItemRequest {
  productId: number
  optionId: number | null
}

export interface ProductBatchRequest {
  items: ProductBatchItemRequest[]
}

export interface ProductBatchOptionResponse {
  id: number
  name: string
  price: number
}

export interface ProductBatchItemResponse {
  id: number
  available: boolean
  name: string | null
  imageUrl: string | null
  originalPrice: number | null
  discountPrice: number | null
  options: ProductBatchOptionResponse[]
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
