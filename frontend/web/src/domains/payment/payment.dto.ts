import type { PaymentCancelCode, PaymentMethod, PaymentStatus } from './payment.types'

export interface PaymentCreateRequest {
  orderId: number
  paymentMethod: PaymentMethod
}

export interface PaymentConfirmRequest {
  paymentKey: string
  pgOrderId: string
  amount: number
}

export interface PaymentCancelRequest {
  cancelReason: string
}

export interface PaymentCancelResponse {
  code: PaymentCancelCode
  message: string
}

export interface PaymentResponse {
  id: number
  orderId: number
  paymentStatus: PaymentStatus
  pgOrderId: string
}
