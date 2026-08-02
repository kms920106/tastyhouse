export type PaymentStatus = 'PENDING' | 'COMPLETED' | 'FAILED' | 'CANCELLED'

export type PaymentMethod =
  | 'CASH_ON_SITE'
  | 'CARD_ON_SITE'
  | 'CREDIT_CARD'
  | 'MOBILE'
  | 'KAKAO_PAY'
  | 'ZERO_PAY'

export type PaymentCancelCode =
  | 'SUCCESS'
  | 'ALREADY_PREPARING'
  | 'ALREADY_CANCELLED'
  | 'ORDER_COMPLETED'
  | 'CANCEL_FAILED'
