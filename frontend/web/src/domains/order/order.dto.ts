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
  /**
   * 수령 예약 시각(슬롯 startAt). null이면 즉시 주문.
   *
   * 서버가 슬롯을 재계산해 대조하므로 임의 시각을 보내면 `ORDER_SCHEDULED_AT_UNAVAILABLE`로 거절된다.
   */
  scheduledAt: string | null
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
  /** 수령 예약 시각(슬롯 시작). null이면 즉시 주문 */
  scheduledAt: string | null
}
