import 'server-only'

import { api } from '@/lib/api'
import type {
  PaymentCancelRequest,
  PaymentCancelResponse,
  PaymentConfirmRequest,
  PaymentCreateRequest,
  PaymentResponse,
} from './payment.dto'

const ENDPOINT = '/api/payments'

export const paymentRepository = {
  // 결제 생성
  async createPayment(request: PaymentCreateRequest) {
    return api.post<PaymentResponse>(`${ENDPOINT}/v1`, request)
  },

  // 현장결제 완료
  async completeOnSitePayment(paymentId: number) {
    return api.post<PaymentResponse>(`${ENDPOINT}/v1/${paymentId}/complete`)
  },

  // 토스 결제 승인
  async confirmPaymentToss(request: PaymentConfirmRequest, accessToken?: string) {
    return api.post<PaymentResponse>(
      `${ENDPOINT}/v1/toss/confirm`,
      request,
      accessToken
        ? {
            headers: {
              Authorization: `Bearer ${accessToken}`,
            },
          }
        : undefined,
    )
  },

  // 결제 취소
  async cancelPayment(paymentId: number, request: PaymentCancelRequest) {
    return api.post<PaymentCancelResponse>(`${ENDPOINT}/v1/${paymentId}/cancel`, request)
  },
}
