'use server'

import type { PaymentCancelCode, PaymentMethod } from '@/domains/payment'
import { paymentRepository } from '@/domains/payment/payment.repository'

export async function createPayment({
  orderId,
  paymentMethod,
}: {
  orderId: number
  paymentMethod: PaymentMethod
}) {
  return paymentRepository.createPayment({ orderId, paymentMethod })
}

export async function getPaymentByOrderId(orderId: number) {
  return paymentRepository.getPaymentByOrderId(orderId)
}

export async function completeOnSitePayment(paymentId: number) {
  return paymentRepository.completeOnSitePayment(paymentId)
}

export async function confirmPaymentToss({
  paymentKey,
  pgOrderId,
  amount,
  accessToken,
}: {
  paymentKey: string
  pgOrderId: string
  amount: number
  accessToken?: string
}) {
  return paymentRepository.confirmPaymentToss({ paymentKey, pgOrderId, amount }, accessToken)
}

export type CancelPaymentResult =
  | { success: true; code: PaymentCancelCode }
  | { success: false; error?: string }

export async function cancelPayment({
  paymentId,
  cancelReason,
}: {
  paymentId: number
  cancelReason: string
}): Promise<CancelPaymentResult> {
  const { error, errorCode, message, data } = await paymentRepository.cancelPayment(paymentId, {
    cancelReason,
  })

  if (error || !data) {
    return errorCode === 'PAYMENT_CANCEL_UNCONFIRMED'
      ? { success: false, error: message ?? error }
      : { success: false }
  }

  return { success: true, code: data.code }
}
