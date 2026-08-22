import { PaginationParams } from '@/types/common'
import type {
  ShopAmenityCode,
  ShopDeliveryTipDayType,
  ShopExtraDeliveryTipType,
  ShopFoodType,
} from '.'
import type { OrderUnavailableReasonCode, ShopOperatingStatus } from './shop.types'
import { ShopAmenity, ShopBreakTime, ShopBusinessHour, ShopClosedDay } from '.'
import type { OrderMethodType } from '../order'
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

/** 사장님 공지. 노출 중인 1건만 내려오며, 없으면 응답 `data` 가 null 이다 */
export interface ShopNoticeResponse {
  id: number
  content: string
  imageUrls: string[]
  createdAt: string
}

export interface ShopProductCategoryResponse {
  categoryName: string
  products: ProductListItemResponse[]
}

export interface ShopBannerListItemResponse {
  id: number
  imageUrl: string
}

/**
 * 메뉴모음컷 1장. 승인된 것만 `sort` 오름차순으로 내려오므로 프론트가 재정렬하지 않는다.
 *
 * `imageUrl` 이 null 일 수 있다 — 원본 파일이 정리된 뒤 레코드만 남은 경우이며, 화면에서는 걸러낸다.
 */
export interface ShopMenuCollectionImageResponse {
  id: number
  imageUrl: string | null
  sort: number
}

/** 주문안내. 미설정이거나 관리자가 게시중단하면 응답 `data` 가 null 이다 */
export interface ShopOrderNoticeResponse {
  content: string
}

/** 인기 메뉴 1건. 사장님 추천 메뉴가 먼저 채워지고 남은 자리를 최근 30일 판매량으로 채운다 */
export interface ShopPopularProductResponse {
  id: number
  name: string
  imageUrl: string | null
  originalPrice: number
  discountPrice: number | null
  discountRate: number | null
  rating: number | null
  reviewCount: number | null
  representative: boolean
  spiciness: number | null
  /** 최근 30일 완료주문 판매 수량. 사장님 추천으로 채워진 항목은 0 일 수 있다 */
  salesQuantity: number
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
  ownerReplyContent: string | null
  ownerReplyCreatedAt: string | null
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

/**
 * 주문방식 1건.
 *
 * `code`/`name` 은 기존 wire 계약이라 `orderMethod`/`orderMethodName` 으로 개명하지 않는다.
 * 주문가능 여부 3필드는 additive 확장이다.
 */
export interface ShopOrderMethodItemResponse {
  code: OrderMethodType
  name: string
  orderable: boolean
  unavailableReason: OrderUnavailableReasonCode | null
  unavailableReasonName: string | null
}

export interface ShopOrderMethodResponse {
  orderMethods: ShopOrderMethodItemResponse[]
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
  /** 예약주문 운영 여부. false면 수령시간 예약 진입 자체를 노출하지 않는다 */
  scheduledOrderEnabled: boolean
  /** 가게 영업 상태. PREPARING이면 지금 주문을 받지 않는다 */
  operatingStatus: ShopOperatingStatus
  /** 주문불가 사유 코드. operatingStatus가 OPEN이면 null */
  unavailableReason: OrderUnavailableReasonCode | null
  /** 서버가 완성해 내려주는 한글 사유 문구. operatingStatus가 OPEN이면 null */
  unavailableReasonName: string | null
}

export interface ScheduledOrderSlotsQuery {
  /** 예약 슬롯을 계산할 주문 방법. DELIVERY / TAKEOUT 외에는 빈 목록이 내려온다 */
  orderMethod: OrderMethodType
}

export interface ScheduledOrderSlotItemResponse {
  startAt: string
  endAt: string
  /** 표시용 문구. 서버가 완성해서 내려주므로 프론트가 조립하지 않는다 */
  label: string
  /** `"오늘"` | `"내일"` */
  dayLabel: string
}

export interface ScheduledOrderSlotsResponse {
  /** 예약주문 운영 중 AND 슬롯 1개 이상 */
  available: boolean
  /** 안내 문구용 리드타임(분). 배달 120 / 포장 60 */
  leadTimeMinutes: number
  /** 슬롯 단위(분). 30 고정 */
  slotUnitMinutes: number
  /** DELIVERY면 범위 슬롯(true), TAKEOUT이면 단일 시각(false) */
  rangeSlot: boolean
  slots: ScheduledOrderSlotItemResponse[]
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

/**
 * 원산지 입력 방식.
 *
 * `DIRECT` 는 점주가 쓴 본문을, `FRANCHISE_URL` 은 본사가 관리하는 링크를 노출한다.
 */
export type ShopOriginSourceType = 'DIRECT' | 'FRANCHISE_URL'

/** 가게 원산지. 미설정이면 응답 `data` 가 null 이고 손님 화면은 영역을 통째로 감춘다 */
export interface ShopOriginResponse {
  sourceType: ShopOriginSourceType
  /** `sourceType === "FRANCHISE_URL"` 이면 null */
  content: string | null
  /** `sourceType === "DIRECT"` 이면 null */
  url: string | null
}

/**
 * 가게 가격 뱃지.
 *
 * **노출 조건 판정은 서버가 한다** — 화면이 배달가·매장가·픽업가를 비교해 판정하면 서버 규정
 * (전체 메뉴 대비 설정 비율, 픽업가 설정 익일 노출 등)과 어긋나 잘못된 뱃지를 보여준다.
 * 화면은 내려온 boolean 두 개만 읽는다.
 */
export interface ShopPriceBadgesResponse {
  /** 매장가격 인증 ON — "매장과 같은 가격" */
  sameAsStorePrice: boolean
  /** 픽업가 ≤ 매장가 조건 충족 — "매장가격 픽업" */
  storePricePickup: boolean
}
