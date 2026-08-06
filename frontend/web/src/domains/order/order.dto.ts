import { PaymentStatus } from '../payment'
import type { OrderMethodType } from './order.types'

export interface OrderProductOptionRequest {
  groupId: number
  optionId: number
}

export interface OrderProductRequest {
  productId: number
  quantity: number
  options: OrderProductOptionRequest[]
}

export interface OrderCreateRequest {
  shopId: number
  orderMethod: OrderMethodType
  orderProducts: OrderProductRequest[]
  memberCouponId: number | null
  usePoint: number
  totalProductAmount: number
  totalDiscountAmount: number
  productDiscountAmount: number
  couponDiscountAmount: number
  finalAmount: number
  request: string
  /**
   * 배달 주소 id. 주문 방법이 DELIVERY면 필수, 그 외에는 null.
   *
   * 좌표는 보내지 않는다 — 서버가 이 id로 저장된 주소에서만 좌표를 읽어 조작을 막는다.
   */
  deliveryAddressId: number | null
  /** 배달팁. 필수이며 배달 외 주문 방법은 0 */
  deliveryTipAmount: number
}

export interface OrderListItemResponse {
  id: number
  shopName: string
  shopThumbnailImageUrl: string
  firstProductName: string
  totalItemCount: number
  amount: number
  paymentStatus: PaymentStatus
  paymentDate: string
}
