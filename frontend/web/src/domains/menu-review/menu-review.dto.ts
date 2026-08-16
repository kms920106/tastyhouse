import type { MenuReviewListItem, MenuReviewWritableItem } from './menu-review.model'

/** `GET /api/menu-reviews/v1/writable/orders/{orderId}` 항목 */
export type MenuReviewWritableItemResponse = MenuReviewWritableItem

/** `GET /api/menu-reviews/v1/products/{productId}` 항목 */
export type MenuReviewListItemResponse = MenuReviewListItem

export interface MenuReviewCreateRequest {
  orderProductId: number
  rating: number
  comment?: string
}

export interface MenuReviewUpdateRequest {
  rating: number
  comment?: string
}
