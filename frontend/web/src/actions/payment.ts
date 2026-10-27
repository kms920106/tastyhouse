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
  | { success: false; unconfirmed: true }
  | { success: false; error?: string }

export async function cancelPayment({
  paymentId,
  cancelReason,
}: {
  paymentId: number
  cancelReason: string
}): Promise<CancelPaymentResult> {
  const { error, errorCode, message, data, status } = await paymentRepository.cancelPayment(
    paymentId,
    { cancelReason },
  )

  // 타임아웃·연결 끊김(status 0)이나 2xx 본문 미수신은 서버가 취소를 끝냈을 수 있어 실패로 단정하지 않는다
  if (status === 0 || (!error && !data)) {
    return { success: false, unconfirmed: true }
  }

  if (error || !data) {
    return errorCode === 'PAYMENT_CANCEL_UNCONFIRMED'
      ? { success: false, error: message ?? error }
      : { success: false }
  }

  return { success: true, code: data.code }
}
