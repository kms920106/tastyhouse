import 'server-only'

import type { OrderMethodType } from '@/domains/order'
import { publicApi } from '@/lib/api'
import { PaginationParams } from '@/types/common'
import {
  ProductBatchRequest,
  ProductBatchResponse,
  ProductDetailResponse,
  ProductNutritionResponse,
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

/**
 * 리뷰 계열 조회용 짧은 캐시.
 *
 * 상품 정보와 달리 리뷰는 회원이 방금 작성하거나 점주가 방금 답변한 결과가 곧바로 보여야 한다.
 * CACHE_OPTIONS(revalidate 3600)를 그대로 쓰면 최대 1시간 동안 작성 이전 스냅샷이 노출된다.
 * 점주 답변은 ceo 앱(별도 Next 인스턴스)에서 등록되므로 web의 revalidatePath로는 무효화할 수 없어,
 * 캐시 수명 자체를 짧게 두는 쪽으로 해결한다. (shop.repository.ts와 같은 이유·같은 값이다.)
 */
const REVIEW_CACHE_OPTIONS = { cache: 'force-cache' as const, next: { revalidate: 60 } }

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
  /**
   * 상품 상세.
   *
   * `orderMethod` 를 함께 보내면 서버가 `prices[].price` 를 그 주문유형 기준으로 해석해 내려준다
   * (배달·테이블·예약은 배달가, 포장은 픽업가·미설정 시 배달가). 미지정이면 서버가 `DELIVERY`
   * 로 본다 — 주문 경로 밖(상품 단독 상세)에서는 주문유형이 없으므로 그 기본값을 그대로 쓴다.
   *
   * **주문유형별 가격 해석을 화면이 하지 않는다.** 주문 접수 시 서버가 클라이언트 금액과 자기
   * 계산을 대조하므로, 화면이 다르게 고르면 `ORDER_PRODUCT_AMOUNT_MISMATCH` 로 거절된다.
   */
  async getProductById(productId: number, orderMethod?: OrderMethodType) {
    return publicApi.get<ProductDetailResponse, { orderMethod?: OrderMethodType }>(
      `${ENDPOINT}/v1/${productId}`,
      { ...CACHE_OPTIONS, params: { orderMethod } },
    )
  },
  // 상품 리뷰 수 조회
  async getProductReviewCount(productId: number) {
    return publicApi.get<ProductReviewCountResponse>(
      `${ENDPOINT}/v1/${productId}/reviews/count`,
      REVIEW_CACHE_OPTIONS,
    )
  },
  // 상품 이미지 목록 조회
  async getProductImages(productId: number) {
    return publicApi.get<ProductImagesResponse>(`${ENDPOINT}/v1/${productId}/images`, CACHE_OPTIONS)
  },
  /**
   * 영양성분·알레르기를 조회한다. 미입력 메뉴는 `data` 가 null 이다.
   *
   * 접힌 상태에서는 부르지 않고 펼칠 때 가져온다 — 대부분의 메뉴가 미입력이라 목록 진입마다
   * 조회하면 낭비다. 점주가 고친 값이 곧 반영되어야 하므로 60초 캐시다.
   */
  async getProductNutrition(productId: number) {
    return publicApi.get<ProductNutritionResponse | null>(`${ENDPOINT}/v1/${productId}/nutrition`, {
      cache: 'force-cache' as const,
      next: { revalidate: 60 },
    })
  },
  // 상품 옵션 조회
  async getProductOptions(productId: number) {
    return publicApi.get<ProductOptionsResponse>(
      `${ENDPOINT}/v1/${productId}/options`,
      CACHE_OPTIONS,
    )
  },
  // 상품 리뷰 통계 조회
  async getProductReviewStatistics(productId: number) {
    return publicApi.get<ProductReviewStatisticsResponse>(
      `${ENDPOINT}/v1/${productId}/reviews/statistics`,
      REVIEW_CACHE_OPTIONS,
    )
  },
  // 상품 리뷰 목록 조회
  async getProductReviews(productId: number, params: ProductReviewListQuery) {
    return publicApi.get<ProductReviewsByRatingResponse>(`${ENDPOINT}/v1/${productId}/reviews`, {
      ...REVIEW_CACHE_OPTIONS,
      params,
    })
  },
}
