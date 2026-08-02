import 'server-only'

import { publicApi } from '@/lib/api'
import { PaginationParams } from '@/types/common'
import {
  ProductBatchRequest,
  ProductBatchResponse,
  ProductDetailResponse,
  ProductImagesResponse,
  ProductOptionsResponse,
  ProductReviewCountResponse,
  ProductReviewListQuery,
  ProductReviewStatisticsResponse,
  ProductReviewsByRatingResponse,
  ProductTodayDiscountListItemResponse,
} from './product.dto'

const ENDPOINT = '/api/products'

const CACHE_OPTIONS = { cache: 'force-cache' as const, next: { revalidate: 3600 } }

export const productRepository = {
  // 오늘의 할인 상품 목록 조회
  async getTodayDiscountProducts(params: PaginationParams) {
    return publicApi.get<ProductTodayDiscountListItemResponse[]>(`${ENDPOINT}/v1/today-discounts`, {
      ...CACHE_OPTIONS,
      params,
    })
  },
  // 상품 배치 조회 (장바구니·주문서 등 여러 상품·옵션을 한 번에 조회)
  async getProductsBatch(body: ProductBatchRequest) {
    return publicApi.post<ProductBatchResponse>(`${ENDPOINT}/v1/batch`, body)
  },
  // 상품 상세 조회
  async getProductById(productId: number) {
    return publicApi.get<ProductDetailResponse>(`${ENDPOINT}/v1/${productId}`, CACHE_OPTIONS)
  },
  // 상품 리뷰 수 조회
  async getProductReviewCount(productId: number) {
    return publicApi.get<ProductReviewCountResponse>(
      `${ENDPOINT}/v1/${productId}/reviews/count`,
      CACHE_OPTIONS,
    )
  },
  // 상품 이미지 목록 조회
  async getProductImages(productId: number) {
    return publicApi.get<ProductImagesResponse>(`${ENDPOINT}/v1/${productId}/images`, CACHE_OPTIONS)
  },
  // 상품 옵션 조회
  async getProductOptions(productId: number) {
    return publicApi.get<ProductOptionsResponse>(`${ENDPOINT}/v1/${productId}/options`, CACHE_OPTIONS)
  },
  // 상품 리뷰 통계 조회
  async getProductReviewStatistics(productId: number) {
    return publicApi.get<ProductReviewStatisticsResponse>(
      `${ENDPOINT}/v1/${productId}/reviews/statistics`,
      CACHE_OPTIONS,
    )
  },
  // 상품 리뷰 목록 조회
  async getProductReviews(productId: number, params: ProductReviewListQuery) {
    return publicApi.get<ProductReviewsByRatingResponse>(`${ENDPOINT}/v1/${productId}/reviews`, {
      ...CACHE_OPTIONS,
      params,
    })
  },
}
