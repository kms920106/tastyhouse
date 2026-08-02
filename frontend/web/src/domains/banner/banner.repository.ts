import 'server-only'

import { publicApi } from '@/lib/api'
import { PaginationParams } from '@/types/common'
import { BannerListItemResponse } from './banner.dto'

const ENDPOINT = '/api/banners'

const CACHE_OPTIONS = { cache: 'force-cache' as const, next: { revalidate: 3600 } }

export const bannerRepository = {
  // 홈 배너 목록 조회
  async getHomeBanners(params: PaginationParams) {
    return publicApi.get<BannerListItemResponse[]>(`${ENDPOINT}/v1/home`, { ...CACHE_OPTIONS, params })
  },

  // 사이드바 배너 목록 조회
  async getSidebarBanners(params: PaginationParams) {
    return publicApi.get<BannerListItemResponse[]>(`${ENDPOINT}/v1/sidebar`, { ...CACHE_OPTIONS, params })
  },
}
