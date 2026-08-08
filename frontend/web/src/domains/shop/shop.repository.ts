import 'server-only'

import { api, publicApi } from '@/lib/api'
import { PaginationParams } from '@/types/common'
import {
  ScheduledOrderSlotsQuery,
  ScheduledOrderSlotsResponse,
  ShopAmenityResponse,
  ShopBannerListItemResponse,
  ShopBestListItemResponse,
  ShopBookmarkResponse,
  ShopChoiceListItemResponse,
  ShopDeliveryTipQuery,
  ShopDeliveryTipResponse,
  ShopDetailResponse,
  ShopFoodTypeListItemResponse,
  ShopInfoResponse,
  ShopLatestListItemResponse,
  ShopOrderMethodResponse,
  ShopPhotoCategoryResponse,
  ShopProductCategoryResponse,
  ShopReviewListQuery,
  ShopReviewStatisticsResponse,
  ShopReviewsByRatingResponse,
  ShopStationListItemResponse,
} from './shop.dto'
import type { ShopMapMarker } from './shop.model'

const ENDPOINT = '/api/shops'

const CACHE_OPTIONS = { cache: 'force-cache' as const, next: { revalidate: 3600 } }

export const shopRepository = {
  async getLatestShops(params: PaginationParams) {
    return publicApi.get<ShopLatestListItemResponse[], PaginationParams>(`${ENDPOINT}/v1/latest`, {
      ...CACHE_OPTIONS,
      params,
    })
  },
  async getBestShops(params: PaginationParams) {
    return publicApi.get<ShopBestListItemResponse[], PaginationParams>(`${ENDPOINT}/v1/best`, {
      ...CACHE_OPTIONS,
      params,
    })
  },
  async getChoiceShops(params: PaginationParams) {
    return publicApi.get<ShopChoiceListItemResponse[], PaginationParams>(
      `${ENDPOINT}/v1/editor-choice`,
      {
        ...CACHE_OPTIONS,
        params,
      },
    )
  },
  async getShopStations() {
    return publicApi.get<ShopStationListItemResponse[]>(`${ENDPOINT}/v1/stations`, CACHE_OPTIONS)
  },
  async getShopFoodTypes() {
    return publicApi.get<ShopFoodTypeListItemResponse[]>(`${ENDPOINT}/v1/food-types`, CACHE_OPTIONS)
  },
  async getShopAmenities() {
    return publicApi.get<ShopAmenityResponse[]>(`${ENDPOINT}/v1/amenities`, CACHE_OPTIONS)
  },
  async getShopBanners(shopId: number) {
    return publicApi.get<ShopBannerListItemResponse[]>(
      `${ENDPOINT}/v1/${shopId}/banners`,
      CACHE_OPTIONS,
    )
  },
  async getShopBookmark(shopId: number) {
    return api.get<ShopBookmarkResponse>(`${ENDPOINT}/v1/${shopId}/bookmark`)
  },
  async toggleShopBookmark(shopId: number) {
    return api.post<ShopBookmarkResponse>(`${ENDPOINT}/v1/${shopId}/bookmark`)
  },
  async getShopInfo(shopId: number) {
    return publicApi.get<ShopInfoResponse>(`${ENDPOINT}/v1/${shopId}/info`, CACHE_OPTIONS)
  },
  async getShopProducts(shopId: number) {
    return publicApi.get<ShopProductCategoryResponse[]>(
      `${ENDPOINT}/v1/${shopId}/products`,
      CACHE_OPTIONS,
    )
  },
  async getShopPhotos(shopId: number) {
    return publicApi.get<ShopPhotoCategoryResponse[]>(
      `${ENDPOINT}/v1/${shopId}/photos`,
      CACHE_OPTIONS,
    )
  },
  async getShopReviewStatistics(shopId: number) {
    return publicApi.get<ShopReviewStatisticsResponse>(
      `${ENDPOINT}/v1/${shopId}/reviews/statistics`,
      CACHE_OPTIONS,
    )
  },
  async getShopReviews(shopId: number, params: ShopReviewListQuery) {
    return publicApi.get<ShopReviewsByRatingResponse>(`${ENDPOINT}/v1/${shopId}/reviews`, {
      ...CACHE_OPTIONS,
      params,
    })
  },
  async getShopOrderMethods(shopId: number) {
    return publicApi.get<ShopOrderMethodResponse>(
      `${ENDPOINT}/v1/${shopId}/order-methods`,
      CACHE_OPTIONS,
    )
  },
  async getMapMarkers(params: { latitude: number; longitude: number }) {
    return publicApi.get<ShopMapMarker[]>(`${ENDPOINT}/v1/map/markers`, { params })
  },
  /**
   * 가게 상세를 조회한다.
   *
   * 목록 API와 달리 CACHE_OPTIONS(force-cache, revalidate 3600)를 쓰지 않는다.
   * 응답에 주문 가능 여부를 가르는 판정 기준(minOrderAmount, operatingStatus)이 포함되어
   * 있어, 값이 낡으면 최소주문금액 차단이 fail-open으로 뒤집힌다.
   * (minOrderAmount가 0으로 낡으면 "제한 없음"으로 판정되어 미달 주문이 통과한다.)
   */
  async getShopDetail(shopId: number) {
    return publicApi.get<ShopDetailResponse>(`${ENDPOINT}/v1/${shopId}`)
  },
  /**
   * 가게 배달팁을 조회한다.
   *
   * 파라미터가 없으면 범위 모드(하한~상한), `deliveryAddressId`·`orderAmount`를 주면 확정 모드다.
   * 시간별 배달팁 때문에 호출 시각에 따라 값이 달라지므로 캐시하지 않는다 — 낡은 값으로 주문하면
   * 서버 재계산과 어긋나 `ORDER_DELIVERY_TIP_AMOUNT_MISMATCH`로 주문이 거절된다.
   *
   * `publicApi`가 아니라 `api`를 쓴다. 확정 모드는 서버가 `deliveryAddressId`로 회원 주소록을
   * 조회해 좌표를 읽으므로 인증이 필요하고, 토큰이 없으면 `deliveryTip`이 `null`로 내려와
   * 결제 화면 배달팁이 0원으로 표기된다. `api`는 토큰이 있을 때만 헤더를 붙이므로
   * 비로그인 범위 모드(가게 카드·상세)도 그대로 동작한다.
   */
  async getShopDeliveryTip(shopId: number, params: ShopDeliveryTipQuery) {
    return api.get<ShopDeliveryTipResponse, ShopDeliveryTipQuery>(
      `${ENDPOINT}/v1/${shopId}/delivery-tip`,
      { params },
    )
  },
  /**
   * 수령시간 예약 가능 슬롯을 조회한다.
   *
   * 배달팁과 같은 이유로 캐시하지 않는다 — 시간이 지나면 리드타임 하한을 넘긴 슬롯이 사라지므로
   * 낡은 목록으로 주문하면 서버 재계산과 어긋나 `ORDER_SCHEDULED_AT_UNAVAILABLE`로 거절된다.
   *
   * `publicApi`가 아니라 `api`를 쓴다. 비로그인 조회도 허용되지만(인증 선택), 토큰이 있으면
   * 그대로 실어 보내는 `api` 쪽이 다른 가게 조회 경로와 일관된다.
   */
  async getScheduledOrderSlots(shopId: number, params: ScheduledOrderSlotsQuery) {
    return api.get<ScheduledOrderSlotsResponse, ScheduledOrderSlotsQuery>(
      `${ENDPOINT}/v1/${shopId}/scheduled-order-slots`,
      { params },
    )
  },
}
