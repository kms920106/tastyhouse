import 'server-only'

import { api, publicApi } from '@/lib/api'

import type {
  PopularKeywordResponse,
  RecommendedKeywordResponse,
  SearchMenuListItemResponse,
  SearchShopListItemResponse,
  SearchQuery,
  SearchReviewListItemResponse,
} from './search.dto'

const ENDPOINT = '/api/search'

export const searchRepository = {
  async getPopularKeywords() {
    return publicApi.get<PopularKeywordResponse[]>(`${ENDPOINT}/v1/popular-keywords`)
  },
  async getRecommendedKeywords() {
    return publicApi.get<RecommendedKeywordResponse[]>(`${ENDPOINT}/v1/recommended-keywords`)
  },
  async searchMenus(params: SearchQuery) {
    return publicApi.get<SearchMenuListItemResponse[], SearchQuery>(`${ENDPOINT}/v1/menus`, {
      params,
    })
  },
  async searchReviews(params: SearchQuery) {
    return publicApi.get<SearchReviewListItemResponse[], SearchQuery>(`${ENDPOINT}/v1/reviews`, {
      params,
    })
  },
  async searchShops(params: SearchQuery) {
    return api.get<SearchShopListItemResponse[], SearchQuery>(`${ENDPOINT}/v1/shops`, {
      params,
    })
  },
  async searchPublicShops(params: SearchQuery) {
    return publicApi.get<SearchShopListItemResponse[], SearchQuery>(
      `${ENDPOINT}/v1/shops/public`,
      { params },
    )
  },
}
