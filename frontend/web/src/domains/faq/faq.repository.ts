import 'server-only'

import { publicApi } from '@/lib/api'
import { FaqCategoryListItemResponse, FaqListItemResponse, FaqListQuery } from './faq.dto'

const ENDPOINT = '/api/faqs'

const CACHE_OPTIONS = { cache: 'force-cache' as const, next: { revalidate: 3600 } }

export const faqRepository = {
  // FAQ 카테고리 목록 조회
  async getFaqCategories() {
    return publicApi.get<FaqCategoryListItemResponse[]>(`${ENDPOINT}/v1/categories`, CACHE_OPTIONS)
  },

  // FAQ 목록 조회
  async getFaqList(params?: FaqListQuery) {
    return publicApi.get<FaqListItemResponse[]>(`${ENDPOINT}/v1`, { ...CACHE_OPTIONS, params })
  },
}
