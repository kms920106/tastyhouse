import { PaginationParams } from '@/types/common'
import type {
  ShopAmenityCode,
  ShopDeliveryTipDayType,
  ShopExtraDeliveryTipType,
  ShopFoodType,
} from '.'
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
  /** 최소주문금액. 0이면 미설정(제한 없음)이며, 배달 주문에만 적용된다. */
  minOrderAmount: number
  /** 배달팁 하한. 0이면 배달팁 없음 */
  minDeliveryTip: number
  /** 배달팁 상한 (고객 주소 확정 전) */
  maxDeliveryTip: number
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
  /** 최소주문금액. 0이면 미설정(제한 없음)이며, 배달 주문에만 적용된다. */
  minOrderAmount: number
  /** 배달팁 하한. 0이면 배달팁 없음 */
  minDeliveryTip: number
  /** 배달팁 상한 (고객 주소 확정 전) */
  maxDeliveryTip: number
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
  /** 최소주문금액. 0이면 미설정(제한 없음)이며, 배달 주문에만 적용된다. */
  minOrderAmount: number
  /** 배달팁 하한. 0이면 배달팁 없음 */
  minDeliveryTip: number
  /** 배달팁 상한 (고객 주소 확정 전) */
  maxDeliveryTip: number
}

export interface ShopDeliveryTipQuery {
  /** 확정 계산용 배달 주소 id. 없으면 범위 모드 */
  deliveryAddressId?: number
  /** 상품 할인 후 금액. 구간 확정용 */
  orderAmount?: number
  /** 주문 방법. 기본 DELIVERY */
  orderMethod?: string
}

export interface ShopDeliveryTipBreakdownItemResponse {
  /** 계산 근거 문구. 서버가 완성해서 내려주므로 프론트가 조립하지 않는다 */
  label: string
  amount: number
}

export interface ShopDeliveryTipTierItemResponse {
  minOrderAmount: number
  tipAmount: number
}

export interface ShopDeliveryTipDistanceResponse {
  baseDistanceMeters: number
  surchargeUnit: string
  surchargeAmount: number
}

export interface ShopDeliveryTipRegionItemResponse {
  /** `"서울특별시 강남구 역삼1동"` 형태로 서버가 완성해서 내려준다 */
  regionName: string
  tipAmount: number
}

export interface ShopDeliveryTipScheduleItemResponse {
  dayType: ShopDeliveryTipDayType
  /** 요일 구분 설명. 서버가 완성해서 내려주므로 프론트가 코드를 라벨로 변환하지 않는다 */
  dayTypeDescription: string
  /** `"HH:mm"` — 영업시간·휴게시간 응답과 같은 포맷이다(ceo-api는 `"HH:mm:ss"`라 다름) */
  startTime: string
  /** `"HH:mm"` */
  endTime: string
  tipAmount: number
}

export interface ShopDeliveryTipResponse {
  /** 확정 배달팁. 확정 불가(주소 미확정 등)면 null */
  deliveryTip: number | null
  minDeliveryTip: number
  maxDeliveryTip: number
  breakdown: ShopDeliveryTipBreakdownItemResponse[]
  tiers: ShopDeliveryTipTierItemResponse[]
  extraTipType: ShopExtraDeliveryTipType
  distance: ShopDeliveryTipDistanceResponse | null
  regions: ShopDeliveryTipRegionItemResponse[]
  schedules: ShopDeliveryTipScheduleItemResponse[]
  /** 공휴일 배달팁. 0이면 미설정 */
  holidayTipAmount: number
}
