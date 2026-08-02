import { env } from '@/lib/env'
import { PAGE_PATHS } from '@/lib/paths'
import { confirmPaymentToss } from '@/actions/payment'
import { NextRequest, NextResponse } from 'next/server'

/**
 * 토스페이먼츠 결제 성공 콜백 API
 *
 * 결제 완료 후 토스페이먼츠에서 리다이렉트되는 엔드포인트
 * Query Parameters:
 * - orderId: PG 주문 ID
 * - paymentKey: 토스페이먼츠 결제 키
 * - amount: 결제 금액
 */
export async function GET(request: NextRequest) {
  const origin = env.NEXT_PUBLIC_SITE_URL

  const searchParams = request.nextUrl.searchParams
  const pgOrderId = searchParams.get('orderId')
  const paymentKey = searchParams.get('paymentKey')
  const amount = searchParams.get('amount')

  // 필수 파라미터 검증
  if (!pgOrderId || !paymentKey || !amount) {
    const failUrl = new URL('/payments/fail', origin)
    failUrl.searchParams.set('message', '필수 파라미터가 누락되었습니다.')
    return NextResponse.redirect(failUrl)
  }

  const amountNumber = Number(amount)
  if (!Number.isInteger(amountNumber) || amountNumber <= 0) {
    const failUrl = new URL('/payments/fail', origin)
    failUrl.searchParams.set('message', '유효하지 않은 금액입니다.')
    return NextResponse.redirect(failUrl)
  }

  try {
    // 백엔드 API로 결제 승인 요청 (백엔드에서 pgOrderId 기반으로 사전 저장된 금액과 재검증)
    const result = await confirmPaymentToss({
      paymentKey,
      pgOrderId,
      amount: amountNumber,
    })

    // API 응답 에러 처리
    if (result.error || !result.data) {
      const failUrl = new URL('/payments/fail', origin)
      failUrl.searchParams.set('message', result.error || '결제 승인에 실패했습니다.')
      return NextResponse.redirect(failUrl)
    }

    // 결제 승인 성공 시 주문 완료 페이지로 리다이렉트
    // 백엔드 응답에서 실제 주문 ID 사용
    const { orderId } = result.data
    if (!orderId) {
      const failUrl = new URL('/payments/fail', origin)
      failUrl.searchParams.set('message', '주문 정보를 찾을 수 없습니다.')
      return NextResponse.redirect(failUrl)
    }

    const successUrl = new URL(PAGE_PATHS.ORDER_COMPLETE(orderId), origin)
    return NextResponse.redirect(successUrl)
  } catch (error) {
    console.error('결제 승인 중 오류 발생:', error)

    const failUrl = new URL('/payments/fail', origin)
    failUrl.searchParams.set('message', '결제 승인 중 오류가 발생했습니다.')
    return NextResponse.redirect(failUrl)
  }
}
