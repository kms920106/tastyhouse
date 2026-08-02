import type { PaymentMethod, PaymentStatus } from '../payment'
import type { OrderMethodType } from './order.types'

export interface OrderMethod {
  code: OrderMethodType
  name: string
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
}

export interface OrderPayment {
  id: number
  approvedAt: string
  paymentMethod: PaymentMethod
  paymentStatus: PaymentStatus
  cardCompany: string | null
  cardNumber: string | null
}
