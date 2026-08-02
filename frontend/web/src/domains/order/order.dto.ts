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
}

export interface OrderCreateResponse {
  id: number
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
