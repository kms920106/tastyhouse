'use client'

import type { MemberCoupon } from '@/domains/member'
import { formatDate } from '@/lib/date'
import { formatNumber } from '@/lib/number'
import { cn } from '@/lib/utils'
import Image from 'next/image'

interface Props {
  memberCoupon: MemberCoupon
}

export default function CouponListItem({ memberCoupon }: Props) {
  const {
    name,
    discountType,
    discountAmount,
    maxDiscountAmount,
    minOrderAmount,
    useStartAt,
    useEndAt,
    daysRemaining,
    expired,
  } = memberCoupon

  return (
    <div className="relative w-full">
      <Image
        src="/images/coupon/coupon.png"
        alt="쿠폰"
        width={690}
        height={285}
        className="w-full h-auto"
      />
      <div className="absolute inset-0 flex">
        <div className="flex flex-col justify-center w-[70%] px-[20px] py-7 overflow-hidden">
          <span className="text-[21px] leading-[21px] text-main">
            {discountType === 'AMOUNT' ? `${formatNumber(discountAmount)}p` : `${discountAmount}%`}
          </span>
          <span className={cn('mt-[15px] text-sm leading-[18px]', expired && 'text-gray-400')}>
            {name}
          </span>
          <div className="flex flex-col gap-1 mt-2.5">
            {minOrderAmount > 0 && (
              <span className="text-xs leading-[14px] text-[#aaaaaa]">
                {formatNumber(minOrderAmount)}원 이상 결제시
              </span>
            )}
            {discountType === 'RATE' && maxDiscountAmount && (
              <span className="text-xs leading-[14px] text-[#aaaaaa]">
                최대 {formatNumber(maxDiscountAmount)}원 할인
              </span>
            )}
            <span className="text-xs leading-[14px] text-[#aaaaaa]">
              {formatDate(useStartAt, 'YYYY-MM-DD')} ~ {formatDate(useEndAt, 'YYYY-MM-DD')}
            </span>
          </div>
        </div>
        <div className="flex items-center justify-center w-[30%]">
          <span
            className={cn(
              'text-base leading-[16px]',
              expired ? 'text-gray-400' : 'text-[#666666]',
            )}
          >
            {expired ? '만료' : `D-${daysRemaining}`}
          </span>
        </div>
      </div>
    </div>
  )
}
