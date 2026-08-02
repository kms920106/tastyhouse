'use server'

import type { ShopMapMarker } from '@/domains/shop'
import { shopRepository } from '@/domains/shop/shop.repository'
import { shopService } from '@/domains/shop/shop.service'

export async function getMapMarkers(params: {
  latitude: number
  longitude: number
}): Promise<ShopMapMarker[]> {
  const response = await shopRepository.getMapMarkers(params)
  return response.data ?? []
}

export async function toggleShopBookmark(shopId: number) {
  return shopRepository.toggleShopBookmark(shopId)
}

export async function getLatestShops({ page, size }: { page: number; size: number }) {
  return shopRepository.getLatestShops({ page, size })
}

export async function getBestShops({ page, size }: { page: number; size: number }) {
  return shopRepository.getBestShops({ page, size })
}

export async function getShopInfo(shopId: number) {
  return shopRepository.getShopInfo(shopId)
}

export async function getShopDetail(shopId: number) {
  return shopRepository.getShopDetail(shopId)
}

export async function getShopMenus(shopId: number) {
  return shopRepository.getShopProducts(shopId)
}

export async function getShopPhotos(shopId: number) {
  return shopRepository.getShopPhotos(shopId)
}

export async function getShopReviewStatistics(shopId: number) {
  return shopRepository.getShopReviewStatistics(shopId)
}

export async function getShopReviews(
  shopId: number,
  { page, size, hasImage }: { page: number; size: number; hasImage?: boolean },
) {
  return shopRepository.getShopReviews(shopId, { page, size, hasImage })
}

export async function getShopFoodTypes() {
  return shopService.getShopFoodTypes()
}
