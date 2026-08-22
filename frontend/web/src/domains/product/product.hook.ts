'use client'

import {
  getProductNutrition,
  getProductOptions,
  getProductReviewStatistics,
  getProductReviews,
  getTodayDiscountProducts,
} from '@/actions/product'
import { useInfiniteQuery, useQuery } from '@tanstack/react-query'

const REVIEWS_PAGE_SIZE = 5
const TODAY_DISCOUNT_PAGE_SIZE = 10

export const productQueryKeys = {
  nutrition: (productId: number) => ['product', productId, 'product-nutrition'] as const,
  options: (productId: number) => ['product', productId, 'product-options'] as const,
  reviewStatistics: (productId: number) =>
    ['product', productId, 'product-review-statistics'] as const,
  reviews: (productId: number) => ['product', productId, 'product-detail-reviews'] as const,
  todayDiscounts: ['product', 'today-discounts'] as const,
}

/**
 * 영양성분·알레르기를 조회합니다.
 *
 * `enabled` 로 지연 로딩합니다 — 대부분의 메뉴가 미입력이라 접힌 상태에서까지 부르면
 * 목록 진입마다 헛된 요청이 쌓입니다. 펼칠 때 `enabled` 를 켜세요.
 */
export function useProductNutrition(productId: number, enabled: boolean) {
  return useQuery({
    queryKey: productQueryKeys.nutrition(productId),
    queryFn: () => getProductNutrition(productId),
    enabled,
  })
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
      const response = await getTodayDiscountProducts({
        page: pageParam,
        size: TODAY_DISCOUNT_PAGE_SIZE,
      })
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
