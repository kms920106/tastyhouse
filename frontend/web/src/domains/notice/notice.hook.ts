'use client'

import { getNoticeList } from '@/actions/notice'
import { useInfiniteQuery } from '@tanstack/react-query'

const PAGE_SIZE = 10

export const noticeQueryKeys = {
  list: ['notices'] as const,
}

export function useNoticeList() {
  return useInfiniteQuery({
    queryKey: noticeQueryKeys.list,
    queryFn: async ({ pageParam }) => {
      const response = await getNoticeList({ page: pageParam, size: PAGE_SIZE })
      if (!response.data) throw new Error('응답 데이터가 없습니다.')
      return response
    },
    initialPageParam: 0,
    getNextPageParam: (lastPage) => {
      if (!lastPage.pagination) return undefined
      const { page, totalPages } = lastPage.pagination
      return page + 1 < totalPages ? page + 1 : undefined
    },
  })
}
