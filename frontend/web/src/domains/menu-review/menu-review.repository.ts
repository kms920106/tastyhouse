import 'server-only'

import { api, publicApi } from '@/lib/api'
import type { PaginationParams } from '@/types/common'

import type {
  MenuReviewCreateRequest,
  MenuReviewListItemResponse,
  MenuReviewUpdateRequest,
  MenuReviewWritableItemResponse,
} from './menu-review.dto'

const ENDPOINT = '/api/menu-reviews'

export const menuReviewRepository = {
  // 주문의 평가 가능 메뉴 목록 조회 (평가 제외 대상은 서버가 이미 걸러 내려준다)
  async getWritableMenuReviews(orderId: number) {
    return api.get<MenuReviewWritableItemResponse[]>(`${ENDPOINT}/v1/writable/orders/${orderId}`)
  },
  // 상품별 메뉴 평가 목록 조회 (공개 조회)
  async getProductMenuReviews(productId: number, params: PaginationParams) {
    return publicApi.get<MenuReviewListItemResponse[], PaginationParams>(
      `${ENDPOINT}/v1/products/${productId}`,
      { params },
    )
  },
  // 메뉴 평가 등록 — 생성된 menuReviewId 반환
  async createMenuReview(request: MenuReviewCreateRequest) {
    return api.post<number>(`${ENDPOINT}/v1`, request)
  },
  // 메뉴 평가 수정
  async updateMenuReview(menuReviewId: number, request: MenuReviewUpdateRequest) {
    return api.put<void>(`${ENDPOINT}/v1/${menuReviewId}`, request)
  },
  // 메뉴 평가 삭제
  async deleteMenuReview(menuReviewId: number) {
    return api.delete<void>(`${ENDPOINT}/v1/${menuReviewId}`)
  },
}
