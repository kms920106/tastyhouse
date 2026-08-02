import type { PaymentMethod } from './payment.types'

export interface PaymentMethodOption {
  type: PaymentMethod
  label: string
  badge?: string
  benefitTitle?: string
  benefitDescription?: string
}
