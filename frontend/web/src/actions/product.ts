'use server'

import { productRepository } from '@/domains/product/product.repository'

export async function getProductsBatch(items: { productId: number; optionId: number | null }[]) {
  return productRepository.getProductsBatch({ items })
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
