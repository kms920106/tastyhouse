import type { PaginationParams } from '@/types/common'

export interface PopularKeywordResponse {
  rank: number
  keyword: string
  newKeyword: boolean
}

export interface RecommendedKeywordResponse {
  keyword: string
}

export interface SearchShopListItemResponse {
  shopId: number
  shopName: string
  stationName: string
  rating: number
  imageUrl: string
  bookmarked: boolean
}

export interface SearchMenuListItemResponse {
  id: number
  shopName: string
  name: string
  imageUrl: string | null
  originalPrice: number
  discountPrice: number
  discountRate: number
  rating: number
  reviewCount: number
  representative: boolean
  spiciness: number
}

export interface SearchReviewListItemResponse {
  id: number
  imageUrl: string
}

export interface SearchQuery extends PaginationParams {
  query: string
}
