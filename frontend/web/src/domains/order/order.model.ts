import type { PaymentMethod, PaymentStatus } from '../payment'
import type { OrderUnavailableReasonCode } from '../shop'
import type { OrderMethodType } from './order.types'

export interface OrderMethod {
  code: OrderMethodType
  name: string
  /** 이 주문방식으로 지금 주문할 수 있는지 */
  orderable: boolean
  /** 화면에 표시하지 않고 사유별 분기에만 쓴다. orderable 이 true 면 null */
  unavailableReason: OrderUnavailableReasonCode | null
  /** 서버가 완성해 내려주는 한글 사유 문구 — 그대로 표시한다. orderable 이 true 면 null */
  unavailableReasonName: string | null
}

export interface OrderProductOption {
  groupId: number
  groupName: string
  optionId: number
  optionName: string
  additionalPrice: number
}

export interface OrderProduct {
  productId: number
  optionKey: string
  name: string
  imageUrl: string
  quantity: number
  salePrice: number
  originalPrice: number
  discountPrice: number
  options: OrderProductOption[]
  available: boolean
}

export interface OrderedProductOption {
  id: number
  optionGroupName: string
  optionName: string
  additionalPrice: number
}

export interface OrderedProduct {
  id: number
  productId: number
  name: string
  imageUrl: string
  quantity: number
  originalPrice: number
  discountPrice: number | null
  totalOptionPrice: number
  totalPrice: number
  options: OrderedProductOption[]
  reviewed: boolean
}

export interface Order {
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

export interface OrderDetail {
  id: number
  orderNumber: string
  paymentStatus: PaymentStatus
  shopName: string
  shopPhoneNumber: string
  ordererName: string
  ordererPhone: string
  ordererEmail: string
  totalProductAmount: number
  productDiscountAmount: number
  couponDiscountAmount: number
  pointDiscountAmount: number
  totalDiscountAmount: number
  finalAmount: number
  usedPoint: number
  earnedPoint: number
  orderProducts: OrderedProduct[]
  payment: OrderPayment
  createdAt: string
  /** 수령 예약 시각(슬롯 시작). null이면 즉시 주문 */
  scheduledAt: string | null
  /** 수령 예약 슬롯 종료 시각. 포장은 scheduledAt과 동일 */
  scheduledSlotEndAt: string | null
}

export interface OrderPayment {
  id: number
  approvedAt: string
  paymentMethod: PaymentMethod
  paymentStatus: PaymentStatus
  cardCompany: string | null
  cardNumber: string | null
}
