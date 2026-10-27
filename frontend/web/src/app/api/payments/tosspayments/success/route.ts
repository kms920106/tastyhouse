import { env } from '@/lib/env'
import { PAGE_PATHS } from '@/lib/paths'
import { confirmPaymentToss } from '@/actions/payment'
import { NextRequest, NextResponse } from 'next/server'

// 프록시·로드밸런서가 본문 없이 돌려주는 게이트웨이 오류 — 요청이 web-api에 도달해 승인됐을 수 있다
const GATEWAY_ERROR_STATUSES = new Set([502, 503, 504])

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

    // 응답을 받지 못함(타임아웃·네트워크 오류, status 0) — 서버가 승인했을 수 있으므로 실패가 아니라 '확인 중'으로 안내
    if (result.status === 0) {
      return redirectToUnconfirmed(origin)
    }

    // 게이트웨이 오류(errorCode 없는 502·503·504) — 승인 여부를 알 수 없으므로 실패가 아니라 '확인 중'으로 안내
    if (!result.errorCode && GATEWAY_ERROR_STATUSES.has(result.status)) {
      return redirectToUnconfirmed(origin)
    }

    // API 응답 에러 처리
    if (result.error) {
      const failUrl = new URL('/payments/fail', origin)
      failUrl.searchParams.set('message', result.error)
      if (result.errorCode) {
        failUrl.searchParams.set('code', result.errorCode)
      }
      return NextResponse.redirect(failUrl)
    }

    // 2xx인데 본문을 읽지 못함(본문을 읽는 도중 타임아웃 등) — 서버는 승인을 끝냈을 수 있으므로 '확인 중'으로 안내
    if (!result.data) {
      return redirectToUnconfirmed(origin)
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

    // 승인 요청이 서버에 도달해 처리됐을 수 있으므로 실패로 단정하지 않는다
    return redirectToUnconfirmed(origin)
  }
}

function redirectToUnconfirmed(origin: string) {
  const failUrl = new URL('/payments/fail', origin)
  failUrl.searchParams.set('code', 'PAYMENT_APPROVAL_UNCONFIRMED')
  failUrl.searchParams.set('message', '결제 승인 응답을 받지 못했습니다. 잠시 후 주문 내역에서 결제 상태를 확인해 주세요.')
  return NextResponse.redirect(failUrl)
}
