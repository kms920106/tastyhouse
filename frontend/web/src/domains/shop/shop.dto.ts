import { PaginationParams } from '@/types/common'
import type { ShopAmenityCode, ShopFoodType } from '.'
import { ShopAmenity, ShopBreakTime, ShopBusinessHour, ShopClosedDay } from '.'
import type { OrderMethod } from '../order'
import { ProductListItemResponse } from '../product'

export interface ShopReviewListQuery extends PaginationParams {
  hasImage?: boolean
}

export interface ShopLatestQuery extends PaginationParams {
  stationId?: number
  foodTypes?: ShopFoodType[]
  amenities?: ShopAmenityCode[]
}

export interface ShopFoodTypeListItemResponse {
  code: ShopFoodType
  name: string
  activeImageUrl: string
  inactiveImageUrl: string
}

export interface ShopStationListItemResponse {
  id: number
  name: string
}

export interface ProductChoiceListItemResponse {
  id: number
  name: string
  shopName: string
  imageUrl: string
  originalPrice: number
  discountPrice: number
  discountRate: number
}

export interface ShopPhotoCategoryResponse {
  name: string
  imageUrls: string[]
}

export interface ShopAmenityResponse {
  code: ShopAmenityCode
  name: string
  activeImageUrl: string
  inactiveImageUrl: string
}

export interface ShopBookmarkResponse {
  bookmarked: boolean
}

export interface ShopInfoResponse {
  closedDays: ShopClosedDay[]
  businessHours: ShopBusinessHour[]
  breakTimes: ShopBreakTime[]
  amenities: ShopAmenity[]
  ownerMessage: string | null
  ownerMessageCreatedAt: string | null
}

export interface ShopProductCategoryResponse {
  categoryName: string
  products: ProductListItemResponse[]
}

export interface ShopBannerListItemResponse {
  id: number
  imageUrl: string
}

export interface ShopReviewListItemResponse {
  id: number
  imageUrls: string[]
  totalRating: number
  content: string
  memberId: number
  memberNickname: string
  memberProfileImageUrl: string | null
  createdAt: string
  productId: number | null
  productName: string | null
}

export interface ShopReviewStatisticsResponse {
  totalRating: number
  totalReviewCount: number
  averageTasteRating: number
  averageAmountRating: number
  averagePriceRating: number
  averageAtmosphereRating: number
  averageKindnessRating: number
  averageHygieneRating: number
  willRevisitPercentage: number
  monthlyReviewCounts: Record<string, number>
  ratingCounts: Record<string, number>
}

export interface ShopReviewsByRatingResponse {
  reviewsByRating: Record<string, ShopReviewListItemResponse[]>
  allReviews: ShopReviewListItemResponse[]
  totalReviewCount: number
}

export interface ShopBestListItemResponse {
  id: number
  name: string
  imageUrl: string
  stationName: string
  rating: number
  foodTypes: ShopFoodType[]
}

export interface ShopChoiceListItemResponse {
  id: number
  name: string
  imageUrl: string
  title: string
  content: string
  products: ProductChoiceListItemResponse[]
}

export interface ShopLatestListItemResponse {
  id: number
  name: string
  imageUrl: string
  stationName: string
  rating: number
  reviewCount: number
  bookmarkCount: number
  createdAt: string
  foodTypes: ShopFoodType[]
}

export interface ShopMapListItemResponse {
  id: number
  name: string
  latitude: number
  longitude: number
}

export interface ShopOrderMethodResponse {
  orderMethods: OrderMethod[]
}

export interface ShopDetailResponse {
  id: number
  name: string
  latitude: number
  longitude: number
  rating: number
  roadAddress: string
  lotAddress: string
  phoneNumber: string
}
