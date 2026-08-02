'use client'

import { getLatestReviews } from '@/actions/review'
import type { ReviewType } from '@/domains/review/review.types'
import { useInfiniteQuery } from '@tanstack/react-query'

const PAGE_SIZE = 10

export const reviewQueryKeys = {
  latest: (reviewType: ReviewType) => ['reviews', 'latest', reviewType] as const,
}

export function useLatestReviews(reviewType: ReviewType) {
  return useInfiniteQuery({
    queryKey: reviewQueryKeys.latest(reviewType),
    queryFn: async ({ pageParam }) => {
      const response = await getLatestReviews({
        page: pageParam as number,
        size: PAGE_SIZE,
        type: reviewType,
      })

      if (response.error) {
        throw new Error(response.error)
      }

      if (!response.data) {
        throw new Error('응답 데이터가 없습니다.')
      }

      return response
    },
    initialPageParam: 0,
    getNextPageParam: (lastPage) => {
      if (!lastPage.pagination) return undefined
      const { page, totalPages } = lastPage.pagination
      return page + 1 < totalPages ? page + 1 : undefined
    },
    retry: 1,
    staleTime: 1000 * 60 * 5,
    gcTime: 1000 * 60 * 10,
  })
}
