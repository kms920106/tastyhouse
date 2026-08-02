import { formatNumber } from '@/lib/number'

interface Props {
  totalProductAmount: number
  totalDiscountAmount: number
  totalProductPaymentAmount: number
}

export default function PaymentSummary({
  totalProductAmount,
  totalDiscountAmount,
  totalProductPaymentAmount,
}: Props) {
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
        <div className="flex justify-between items-center">
          <span className="text-sm leading-[14px]">결제예정금액</span>
          <span className="text-main">{formatNumber(totalProductPaymentAmount)}원</span>
        </div>
      </div>
    </div>
  )
}
