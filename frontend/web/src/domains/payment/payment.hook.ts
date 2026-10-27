'use client'

import { getPaymentByOrderId } from '@/actions/payment'
import { useQuery, useQueryClient } from '@tanstack/react-query'
import { useState } from 'react'
import type { PaymentStatus } from './payment.types'

// 취소 요청 시작부터 약 35초(타임아웃 20초 + 3초 간격 5회)까지 확인한다.
// 서버 취소 최악값은 PG 취소 15초 + 결과 불명 시 확인 조회 15초 + 잠금 대기라, 여유는 약 5초다
const CANCEL_RESULT_CHECK_INTERVAL_MS = 3000
const CANCEL_RESULT_CHECK_MAX_ATTEMPTS = 6

export type CancelResultCheckStatus = 'idle' | 'checking' | 'cancelled' | 'unconfirmed'

interface CancelResultCheckData {
  attempt: number
  paymentStatus: PaymentStatus | null
}

export const paymentQueryKeys = {
  cancelResultCheck: (orderId: number, runId: number) =>
    ['payment', 'order', orderId, 'cancel-result', runId] as const,
}

function isCheckFinished(data: CancelResultCheckData | undefined) {
  if (!data) return false
  return data.paymentStatus === 'CANCELLED' || data.attempt >= CANCEL_RESULT_CHECK_MAX_ATTEMPTS
}

// 취소 결과를 모를 때 결제 상태를 다시 조회해 취소 여부를 확정한다
export function useCancelResultCheck(orderId: number, enabled: boolean): CancelResultCheckStatus {
  const queryClient = useQueryClient()
  // enabled가 켜질 때마다 새 queryKey로 시작해, 이전 확인의 attempt·결과가 다음 확인에 이어지지 않게 한다
  const [runId, setRunId] = useState(0)
  const [prevEnabled, setPrevEnabled] = useState(enabled)
  if (enabled !== prevEnabled) {
    setPrevEnabled(enabled)
    if (enabled) setRunId((id) => id + 1)
  }

  const { data } = useQuery({
    queryKey: paymentQueryKeys.cancelResultCheck(orderId, runId),
    queryFn: async ({ queryKey }): Promise<CancelResultCheckData> => {
      // attempt가 매번 달라 같은 응답이 반복돼도 재렌더되고, 조회가 throw해도 1회로 센다
      const attempt = (queryClient.getQueryData<CancelResultCheckData>(queryKey)?.attempt ?? 0) + 1
      const result = await getPaymentByOrderId(orderId).catch(() => null)
      return { attempt, paymentStatus: result?.data?.paymentStatus ?? null }
    },
    enabled,
    retry: false,
    staleTime: 0,
    gcTime: 0,
    refetchIntervalInBackground: true,
    refetchInterval: (query) =>
      isCheckFinished(query.state.data) ? false : CANCEL_RESULT_CHECK_INTERVAL_MS,
  })

  if (!enabled) return 'idle'
  if (data?.paymentStatus === 'CANCELLED') return 'cancelled'
  if (isCheckFinished(data)) return 'unconfirmed'
  return 'checking'
}
