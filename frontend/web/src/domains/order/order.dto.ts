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
  /**
   * 일회용컵 보증금 합계. Σ(옵션.depositAmount) × 수량으로 클라이언트가 계산해 보내고,
   * 서버가 옵션 스냅샷 기준으로 재계산해 대조한다(불일치 시 ORDER_CUP_DEPOSIT_AMOUNT_MISMATCH).
   * 최소주문금액·쿠폰·포인트 기준액에는 포함되지 않는다.
   */
  cupDepositAmount: number
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
