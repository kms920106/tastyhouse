'use client'

import {
  getProductOptions,
  getProductReviewStatistics,
  getProductReviews,
  getTodayDiscountProducts,
} from '@/actions/product'
import { useInfiniteQuery, useQuery } from '@tanstack/react-query'

const REVIEWS_PAGE_SIZE = 5
const TODAY_DISCOUNT_PAGE_SIZE = 10

export const productQueryKeys = {
  options: (productId: number) => ['product', productId, 'product-options'] as const,
  reviewStatistics: (productId: number) => ['product', productId, 'product-review-statistics'] as const,
  reviews: (productId: number) => ['product', productId, 'product-detail-reviews'] as const,
  todayDiscounts: ['product', 'today-discounts'] as const,
}

export function useProductOptions(productId: number) {
  return useQuery({
    queryKey: productQueryKeys.options(productId),
    queryFn: () => getProductOptions(productId),
  })
}

export function useProductReviewStatistics(productId: number) {
  return useQuery({
    queryKey: productQueryKeys.reviewStatistics(productId),
    queryFn: () => getProductReviewStatistics(productId),
  })
}

export function useProductReviews(productId: number) {
  return useQuery({
    queryKey: productQueryKeys.reviews(productId),
    queryFn: () => getProductReviews(productId, { page: 0, size: REVIEWS_PAGE_SIZE }),
  })
}

export function useTodayDiscountProducts() {
  return useInfiniteQuery({
    queryKey: productQueryKeys.todayDiscounts,
    queryFn: async ({ pageParam }) => {
      const response = await getTodayDiscountProducts({ page: pageParam, size: TODAY_DISCOUNT_PAGE_SIZE })
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
