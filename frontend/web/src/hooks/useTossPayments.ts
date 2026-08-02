'use client'

import type { TossPaymentsPayment } from '@tosspayments/tosspayments-sdk'
import { loadTossPayments } from '@tosspayments/tosspayments-sdk'
import { useEffect, useState } from 'react'
import { env } from '@/lib/env'

const TOSS_CLIENT_KEY = env.NEXT_PUBLIC_TOSSPAY_CLIENT_KEY
const TOSS_CUSTOMER_KEY = env.NEXT_PUBLIC_TOSSPAY_CUSTOMER_KEY

export function useTossPayments() {
  const [tossPayment, setTossPayment] = useState<TossPaymentsPayment | null>(null)

  useEffect(() => {
    async function initTossPayments() {
      try {
        const tossPayments = await loadTossPayments(TOSS_CLIENT_KEY)
        const payment = tossPayments.payment({ customerKey: TOSS_CUSTOMER_KEY })
        setTossPayment(payment)
      } catch (error) {
        console.error('토스페이먼츠 초기화 실패:', error)
      }
    }

    initTossPayments()
  }, [])

  return { tossPayment }
}
