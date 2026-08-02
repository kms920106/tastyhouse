import type { MemberCoupon } from '@/domains/member'
import CouponSelector from './CouponSelector'
import PointSelector from './PointSelector'

interface Props {
  availableCoupons: MemberCoupon[]
  totalProductAmount: number
  totalProductDiscountAmount: number
  selectedCoupon: MemberCoupon | null
  onCouponSelect: (coupon: MemberCoupon | null) => void
  availablePoints: number
  pointInput: string
  onPointInputChange: (value: string) => void
}

export default function DiscountApplicationSection({
  availableCoupons,
  totalProductAmount,
  totalProductDiscountAmount,
  selectedCoupon,
  onCouponSelect,
  availablePoints,
  pointInput,
  onPointInputChange,
}: Props) {
  return (
    <div className="px-[15px] py-5">
      <div className="pb-[30px]">
        <h2 className="text-base leading-[16px]">쿠폰/적립금 사용</h2>
      </div>
      <div className="space-y-5">
        <CouponSelector
          availableCoupons={availableCoupons}
          totalProductAmount={totalProductAmount}
          totalProductDiscountAmount={totalProductDiscountAmount}
          selectedCoupon={selectedCoupon}
          onCouponSelect={onCouponSelect}
        />
        <PointSelector
          availablePoints={availablePoints}
          pointInput={pointInput}
          onPointInputChange={onPointInputChange}
        />
      </div>
    </div>
  )
}
