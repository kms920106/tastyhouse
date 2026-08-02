import 'server-only'

import { publicApi } from '@/lib/api'
import { PaginationParams } from '@/types/common'

import { NoticeListItemResponse } from './notice.dto'

const ENDPOINT = '/api/notices'

const CACHE_OPTIONS = { cache: 'force-cache' as const, next: { revalidate: 3600 } }

export const noticeRepository = {
  // 공지사항 목록 조회
  async getNoticeList(params: PaginationParams) {
    return publicApi.get<NoticeListItemResponse[]>(`${ENDPOINT}/v1`, { ...CACHE_OPTIONS, params })
  },
}
