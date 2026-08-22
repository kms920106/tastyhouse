import type { MemberCoupon } from '@/domains/member'
import type { OrderMethodType, OrderProduct } from '@/domains/order'

export interface PaymentSummary {
  totalDiscountAmount: number
  couponDiscount: number
  pointsUsed: number
  /** 배달팁. 할인과 달리 결제 금액에 가산되는 항목이다 */
  deliveryTip: number
  /** 일회용컵 보증금. 배달팁과 마찬가지로 할인 기준액에는 포함되지 않는 가산 항목이다 */
  cupDepositAmount: number
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
 * 일회용컵 보증금 합계를 계산합니다.
 *
 * 계산식은 Σ(옵션.depositAmount) × 수량이며, 서버가 옵션 스냅샷 기준으로 동일하게 재계산해
 * 대조한다(ORDER_CUP_DEPOSIT_AMOUNT_MISMATCH). totalProductAmount에는 포함하지 않는다 —
 * 포함하면 최소주문금액·쿠폰·포인트 기준액까지 오염된다.
 *
 * @param items - 옵션 정보를 포함한 상품 목록
 * @returns 계산된 보증금 합계
 */
export function calculateCupDepositAmount(items: OrderProduct[]): number {
  return items.reduce((sum, item) => {
    const depositPerUnit = item.options.reduce(
      (optionSum, option) => optionSum + (option.depositAmount ?? 0),
      0,
    )
    return sum + depositPerUnit * item.quantity
  }, 0)
}

/**
 * 개인컵 할인 합계를 계산합니다.
 *
 * 개인컵 할인은 보증금이 아니라 상품 할인 축이므로 productDiscountAmount에 가산된다.
 * 서버가 ORDER_PRODUCT_DISCOUNT_AMOUNT_MISMATCH로 대조하므로 함께 계산해야 한다.
 *
 * @param items - 옵션 정보를 포함한 상품 목록
 * @returns 계산된 개인컵 할인 합계
 */
export function calculatePersonalCupDiscountAmount(items: OrderProduct[]): number {
  return items.reduce((sum, item) => {
    const discountPerUnit = item.options.reduce(
      (optionSum, option) => optionSum + (option.personalCupDiscountAmount ?? 0),
      0,
    )
    return sum + discountPerUnit * item.quantity
  }, 0)
}

/**
 * 가게 최소주문금액까지 부족한 금액을 계산합니다.
 *
 * 판정 기준은 상품 할인까지 반영한 금액(쿠폰·포인트 차감 전)으로, 서버의 검증 기준과 동일합니다.
 * 최소주문금액이 미설정(0)이거나 배달 외 주문방식이면 항상 0을 반환합니다 — 픽업(포장)에는
 * 가게 최소주문금액이 적용되지 않습니다.
 * 배달팁은 이 판정에 포함하지 않습니다 — 포함하면 팁이 비싼 가게일수록 최소주문 문턱이 낮아지는
 * 역설이 생기고, 서버의 판정 기준과도 어긋납니다.
 *
 * @param productPaymentAmount - 상품 할인 후 금액
 * @param minOrderAmount - 가게 최소주문금액 (0이면 미설정)
 * @param orderMethod - 주문 방식
 * @returns 부족한 금액. 0이면 최소주문금액을 충족했거나 적용 대상이 아님
 */
export function calculateMinOrderShortfall(
  productPaymentAmount: number,
  minOrderAmount: number,
  orderMethod: OrderMethodType,
): number {
  if (minOrderAmount <= 0 || orderMethod !== 'DELIVERY') {
    return 0
  }

  return Math.max(minOrderAmount - productPaymentAmount, 0)
}

/**
 * 결제 금액을 계산합니다.
 *
 * 배달팁·일회용컵 보증금은 **가산 항목**이므로 최종 결제 금액에만 더합니다. 쿠폰 할인 상한
 * (`amountAfterProductDiscount`)과 포인트 사용 상한(`amountAfterCoupon`)의 기준 금액에는 절대
 * 포함하지 않습니다 — 서버도 같은 기준으로 항목별 대조를 하므로, 여기에 더하면 정상 주문이
 * `ORDER_*_MISMATCH`로 전량 거절됩니다. 최소주문금액 판정도 같은 이유로 이 두 항목을 제외합니다.
 *
 * @param productTotal - 상품 총액
 * @param productDiscount - 상품 할인 총액(개인컵 할인 포함 — 보증금이 아니라 상품 할인 축이다)
 * @param deliveryTip - 배달팁 (배달 외 주문 방법은 0)
 * @param selectedCoupon - 선택된 쿠폰 (선택 사항)
 * @param pointInput - 사용할 포인트 입력값
 * @param cupDepositAmount - 일회용컵 보증금 합계 (보증금 옵션이 없으면 0)
 * @returns 상품 할인, 쿠폰 할인, 사용 포인트, 배달팁, 보증금, 최종 결제 금액을 포함한 객체
 */
export function calculatePaymentSummary(
  productTotal: number,
  productDiscount: number,
  deliveryTip: number,
  selectedCoupon: MemberCoupon | null,
  pointInput: string,
  cupDepositAmount: number = 0,
): PaymentSummary {
  // 상품 금액에서 상품 할인을 제외한 금액 (배달팁·보증금을 더하지 않는다 — 쿠폰 할인 상한의 기준)
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

  // 포인트 사용 상한의 기준 금액. 여기에도 배달팁·보증금을 더하지 않는다
  const amountAfterCoupon = amountAfterProductDiscount - couponDiscount
  const pointsUsed = Math.min(Math.max(parseInt(pointInput) || 0, 0), amountAfterCoupon)

  const totalDiscount = productDiscount + couponDiscount + pointsUsed
  const paymentAmount = Math.max(
    productTotal + deliveryTip + cupDepositAmount - totalDiscount,
    0,
  )

  return {
    totalDiscountAmount: totalDiscount,
    couponDiscount,
    pointsUsed,
    deliveryTip,
    cupDepositAmount,
    paymentAmount,
  }
}
