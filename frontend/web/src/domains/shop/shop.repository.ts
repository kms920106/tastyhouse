import 'server-only'

import { api, publicApi } from '@/lib/api'
import { PaginationParams } from '@/types/common'
import {
  ShopAmenityResponse,
  ShopBannerListItemResponse,
  ShopBestListItemResponse,
  ShopBookmarkResponse,
  ShopChoiceListItemResponse,
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
  async getShopDetail(shopId: number) {
    return publicApi.get<ShopDetailResponse>(`${ENDPOINT}/v1/${shopId}`, CACHE_OPTIONS)
  },
}
