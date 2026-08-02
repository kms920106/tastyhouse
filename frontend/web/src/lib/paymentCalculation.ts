import type { MemberCoupon } from '@/domains/member'
import type { OrderProduct } from '@/domains/order'

export interface PaymentSummary {
  totalDiscountAmount: number
  couponDiscount: number
  pointsUsed: number
  paymentAmount: number
}

/**
 * 상품 총액을 계산합니다.
 *
 * @param items - 가격과 수량 정보를 포함한 상품 목록
 * @returns 계산된 상품 총액
 */
export function calculateTotalProductAmount(items: OrderProduct[]): number {
  return items.reduce((sum, item) => sum + item.originalPrice * item.quantity, 0)
}

/**
 * 상품 할인 총액을 계산합니다.
 *
 * @param items - 할인 금액과 수량 정보를 포함한 상품 목록
 * @returns 계산된 상품 할인 총액
 */
export function calculateTotalProductDiscount(items: OrderProduct[]): number {
  return items.reduce((sum, item) => sum + item.discountPrice * item.quantity, 0)
}

/**
 * 상품 결제 총액을 계산합니다.
 *
 * @param items - 결제 금액과 수량 정보를 포함한 상품 목록
 * @returns 계산된 상품 결제 총액
 */
export function calculateTotalProductPaymentAmount(items: OrderProduct[]): number {
  return calculateTotalProductAmount(items) - calculateTotalProductDiscount(items)
}

/**
 * 결제 금액을 계산합니다.
 *
 * @param productTotal - 상품 총액
 * @param productDiscount - 상품 할인 총액
 * @param selectedCoupon - 선택된 쿠폰 (선택 사항)
 * @param pointInput - 사용할 포인트 입력값
 * @returns 상품 할인, 배송 할인, 쿠폰 할인, 사용 포인트, 최종 결제 금액을 포함한 객체
 */
export function calculatePaymentSummary(
  productTotal: number,
  productDiscount: number,
  selectedCoupon: MemberCoupon | null,
  pointInput: string,
): PaymentSummary {
  // 상품 금액에서 상품 할인을 제외한 금액
  const amountAfterProductDiscount = productTotal - productDiscount

  const rawCouponDiscount = selectedCoupon
    ? selectedCoupon.discountType === 'AMOUNT'
      ? selectedCoupon.discountAmount
      : Math.min(
          Math.floor((amountAfterProductDiscount * selectedCoupon.discountAmount) / 100),
          selectedCoupon.maxDiscountAmount || Infinity,
        )
    : 0
  const couponDiscount = Math.min(rawCouponDiscount, amountAfterProductDiscount)

  const amountAfterCoupon = amountAfterProductDiscount - couponDiscount
  const pointsUsed = Math.min(Math.max(parseInt(pointInput) || 0, 0), amountAfterCoupon)

  const totalDiscount = productDiscount + couponDiscount + pointsUsed
  const paymentAmount = Math.max(productTotal - totalDiscount, 0)

  return {
    totalDiscountAmount: totalDiscount,
    couponDiscount,
    pointsUsed,
    paymentAmount,
  }
}
