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

// 승인(연결 5초+읽기 10초)과 조회 대사(15초)를 합친 PG 호출 최악값 30초에 여유를 더한 값.
// 같은 결제의 취소와 겹쳐 행 잠금 대기가 생기면 넘을 수 있으며, 그때는 '확인 중' 화면으로 간다
const CONFIRM_TIMEOUT_MS = 35000

// PG 취소 1회(연결 5초+읽기 10초) 최악값 15초에 반영 단계의 행 잠금 대기 여유를 더한 값.
// 기본 3초를 쓰면 PG 취소가 늦을 때 backend는 환불을 끝냈는데 web은 실패로 보여준다.
// 결과 불명이라 확인 조회(15초)가 이어 붙으면 넘을 수 있으며, 그때는 '확인 중' 화면으로 간다
const CANCEL_TIMEOUT_MS = 20000

export const paymentRepository = {
  // 결제 생성. 서버는 생성된 결제 ID를 스칼라로 반환한다(ApiResponse<Long>).
  async createPayment(request: PaymentCreateRequest) {
    return api.post<number>(`${ENDPOINT}/v1`, request)
  },

  // 주문별 결제 조회. 결제 생성 응답이 ID뿐이라 PG 결제창에 넘길 pgOrderId는 여기서 받는다.
  async getPaymentByOrderId(orderId: number) {
    return api.get<PaymentResponse>(`${ENDPOINT}/v1/order/${orderId}`)
  },

  // 토스 결제 승인
  async confirmPaymentToss(request: PaymentConfirmRequest, accessToken?: string) {
    return api.post<PaymentResponse>(`${ENDPOINT}/v1/toss/confirm`, request, {
      timeout: CONFIRM_TIMEOUT_MS,
      ...(accessToken ? { headers: { Authorization: `Bearer ${accessToken}` } } : {}),
    })
  },

  // 결제 취소
  async cancelPayment(paymentId: number, request: PaymentCancelRequest) {
    return api.post<PaymentCancelResponse>(`${ENDPOINT}/v1/${paymentId}/cancel`, request, {
      timeout: CANCEL_TIMEOUT_MS,
    })
  },
}
