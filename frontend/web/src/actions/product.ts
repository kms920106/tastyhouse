'use server'

import type { OrderMethodType } from '@/domains/order'
import { productRepository } from '@/domains/product/product.repository'

/**
 * 상품 배치 조회.
 *
 * `orderMethod` 를 함께 보내면 응답 `prices[].price` 가 그 주문유형 기준으로 해석된 값으로 내려온다
 * (배달·테이블·예약은 배달가, 포장은 픽업가·미설정 시 배달가). 장바구니·주문서는 자기 주문유형을
 * 알고 있으므로 항상 보낸다 — 생략하면 서버가 `DELIVERY` 로 보므로 포장 주문에서 표시가 어긋난다.
 */
export async function getProductsBatch(
  items: { productId: number; optionId: number | null }[],
  orderMethod?: OrderMethodType,
) {
  return productRepository.getProductsBatch({ items, orderMethod })
}

export async function getProductById(productId: number) {
  return productRepository.getProductById(productId)
}

export async function getProductImages(productId: number) {
  return productRepository.getProductImages(productId)
}

export async function getProductOptions(productId: number) {
  return productRepository.getProductOptions(productId)
}

export async function getProductNutrition(productId: number) {
  return productRepository.getProductNutrition(productId)
}

export async function getProductReviewStatistics(productId: number) {
  return productRepository.getProductReviewStatistics(productId)
}

export async function getProductReviews(
  productId: number,
  query: { page: number; size: number; hasImage?: boolean },
) {
  return productRepository.getProductReviews(productId, query)
}

export async function getProductReviewCount(productId: number) {
  return productRepository.getProductReviewCount(productId)
}

export async function getTodayDiscountProducts({ page, size }: { page: number; size: number }) {
  return productRepository.getTodayDiscountProducts({ page, size })
}
