import { formatNumber } from '@/lib/number'

interface Props {
  totalProductAmount: number
  totalDiscountAmount: number
  totalProductPaymentAmount: number
  /** 배달팁 하한. 상한과 함께 0이면 배달팁 행을 노출하지 않는다 */
  minDeliveryTip: number
  /** 배달팁 상한 (고객 주소 확정 전) */
  maxDeliveryTip: number
  /** 배달팁 상세 안내 팝업 열기 */
  onDeliveryTipClick: () => void
}

export default function PaymentSummary({
  totalProductAmount,
  totalDiscountAmount,
  totalProductPaymentAmount,
  minDeliveryTip,
  maxDeliveryTip,
  onDeliveryTipClick,
}: Props) {
  // 장바구니에서는 배달 주소가 확정되지 않아 배달팁이 범위로만 표기된다.
  // 결제예정금액에 더하지 않는 것도 같은 이유다 — 확정값은 결제 화면에서 계산한다.
  const deliveryTipLabel =
    minDeliveryTip === maxDeliveryTip
      ? `${formatNumber(minDeliveryTip)}원`
      : `${formatNumber(minDeliveryTip)}~${formatNumber(maxDeliveryTip)}원`

  return (
    <div className="px-[15px] py-5 border-t-8 border-[#f5f5f5] box-border">
      <div className="space-y-5">
        <div className="flex justify-between">
          <span className="text-sm leading-[14px]">상품금액</span>
          <span className="text-sm leading-[14px]">{formatNumber(totalProductAmount)}원</span>
        </div>
        <div className="flex justify-between">
          <span className="text-sm leading-[14px]">상품할인금액</span>
          <span className="text-sm leading-[14px]">
            {totalDiscountAmount > 0 ? '-' : ''}
            {formatNumber(totalDiscountAmount)}원
          </span>
        </div>
        {(minDeliveryTip > 0 || maxDeliveryTip > 0) && (
          <div className="flex justify-between items-center">
            <button
              type="button"
              className="text-sm leading-[14px] underline cursor-pointer"
              onClick={onDeliveryTipClick}
            >
              배달팁
            </button>
            <span className="text-sm leading-[14px]">{deliveryTipLabel}</span>
          </div>
        )}
        <div className="flex justify-between items-center">
          <span className="text-sm leading-[14px]">결제예정금액</span>
          <span className="text-main">{formatNumber(totalProductPaymentAmount)}원</span>
        </div>
      </div>
    </div>
  )
}
