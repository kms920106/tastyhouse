import type { PaymentMethod, PaymentStatus } from './payment.types'
import type { PaymentMethodOption } from './payment.model'

const PAYMENT_STATUS_NAMES: Record<PaymentStatus, string> = {
  PENDING: '결제 대기',
  COMPLETED: '결제 완료',
  FAILED: '결제 실패',
  CANCELLED: '결제 취소',
}

const PAYMENT_STATUS_COLORS: Record<PaymentStatus, string> = {
  PENDING: '#4f9857',
  COMPLETED: '#4f9857',
  FAILED: '#bc4040',
  CANCELLED: '#bc4040',
}

export const PAYMENT_METHODS: PaymentMethodOption[] = [
  {
    type: 'CASH_ON_SITE',
    label: '현장에서 현금 결제',
    badge: '혜택',
    benefitTitle: '현장에서 현금 결제',
    benefitDescription: '현장(가게)에서 현금으로 결제시 최대 10% 포인트 적립',
  },
  {
    type: 'CARD_ON_SITE',
    label: '현장에서 카드 결제',
    badge: '혜택',
    benefitTitle: '현장에서 카드 결제',
    benefitDescription: '현장(가게)에서 카드로 결제시 최대 10% 포인트 적립',
  },
  { type: 'CREDIT_CARD', label: '신용카드' },
  { type: 'MOBILE', label: '휴대폰 결제' },
  // { type: 'KAKAO_PAY', label: '카카오페이' },
  // { type: 'ZERO_PAY', label: '제로페이' },
]

export const getPaymentMethodName = (paymentMethod: PaymentMethod): string => {
  return PAYMENT_METHODS.find((m) => m.type === paymentMethod)?.label || paymentMethod
}

export const getPaymentStatusName = (paymentStatus: PaymentStatus): string => {
  return PAYMENT_STATUS_NAMES[paymentStatus]
}

export const getPaymentStatusColor = (paymentStatus: PaymentStatus): string => {
  return PAYMENT_STATUS_COLORS[paymentStatus]
}
