'use client'

import AppPrimaryButton from '@/components/ui/AppPrimaryButton'
import { toast } from '@/components/ui/AppToaster'
import {
  Drawer,
  DrawerContent,
  DrawerDescription,
  DrawerTitle,
  DrawerTrigger,
} from '@/components/ui/shadcn/drawer'
import type { MemberCoupon } from '@/domains/member'
import { formatNumber } from '@/lib/number'
import { cn } from '@/lib/utils'
import Image from 'next/image'
import { useState } from 'react'

interface Props {
  availableCoupons: MemberCoupon[]
  totalProductAmount: number
  totalProductDiscountAmount: number
  selectedCoupon: MemberCoupon | null
  onCouponSelect: (coupon: MemberCoupon | null) => void
}

export default function CouponSelector({
  availableCoupons,
  totalProductAmount,
  totalProductDiscountAmount,
  selectedCoupon,
  onCouponSelect,
}: Props) {
  const [couponDrawerOpen, setCouponDrawerOpen] = useState(false)
  const [tempSelectedCoupon, setTempSelectedCoupon] = useState<MemberCoupon | null>(
    null,
  )

  // 상품 금액에서 상품 할인을 제외한 금액
  const amountAfterProductDiscount = totalProductAmount - totalProductDiscountAmount

  const handleDrawerOpen = () => {
    setTempSelectedCoupon(selectedCoupon)
  }

  const handleApplyCoupon = () => {
    // 이미 선택된 쿠폰을 다시 클릭한 경우 취소
    if (tempSelectedCoupon && selectedCoupon?.id === tempSelectedCoupon.id) {
      onCouponSelect(null)
      setCouponDrawerOpen(false)
      return
    }

    if (tempSelectedCoupon && tempSelectedCoupon.minOrderAmount > amountAfterProductDiscount) {
      toast(`${formatNumber(tempSelectedCoupon.minOrderAmount)}원 이상 결제 시 사용 가능합니다.`)
      return
    }
    onCouponSelect(tempSelectedCoupon)
    setCouponDrawerOpen(false)
  }

  const getButtonText = () => {
    if (tempSelectedCoupon && selectedCoupon?.id === tempSelectedCoupon.id) {
      return '사용 취소하기'
    }
    return '사용하기'
  }

  if (availableCoupons.length === 0) {
    return (
      <div>
        <h3 className="text-xs leading-[12px] mb-2.5">쿠폰</h3>
        <button className="w-full h-[50px] px-[15px] py-[17px] flex items-center justify-between border border-line box-border">
          <span className="text-sm leading-[14px] text-[#aaaaaa]">
            사용할 수 있는 쿠폰이 없습니다.
          </span>
          <Image src="/images/layout/nav-right.png" alt="닫기" width={9} height={16} />
        </button>
      </div>
    )
  }

  return (
    <div>
      <h3 className="text-xs leading-[12px] mb-2.5">쿠폰</h3>
      <Drawer open={couponDrawerOpen} onOpenChange={setCouponDrawerOpen}>
        <DrawerTrigger asChild>
          <button
            onClick={(e) => {
              e.currentTarget.blur()
              handleDrawerOpen()
            }}
            className="w-full h-[50px] px-[15px] py-[17px] flex items-center justify-between border box-border border-line"
          >
            <span
              className={cn('text-sm leading-[14px]', selectedCoupon ? 'text-black' : 'text-[#aaaaaa]')}
            >
              {selectedCoupon
                ? selectedCoupon.discountType === 'AMOUNT'
                  ? `${selectedCoupon.name} (${formatNumber(selectedCoupon.discountAmount)}원)`
                  : `${selectedCoupon.name} (${selectedCoupon.discountAmount}%)`
                : '쿠폰을 선택해 주세요.'}
            </span>
            <Image src="/images/layout/nav-right.png" alt="선택" width={9} height={16} />
          </button>
        </DrawerTrigger>
        <DrawerContent className="rounded-t-[20px]">
          <div className="px-[15px] py-5">
            <DrawerTitle className="sr-only">쿠폰</DrawerTitle>
            <DrawerDescription className="sr-only">쿠폰 선택 목록</DrawerDescription>
            <div className="space-y-4">
              {availableCoupons.map((coupon) => {
                const isSelected = tempSelectedCoupon?.id === coupon.id
                return (
                  <div
                    key={coupon.id}
                    className={cn(
                      'relative overflow-hidden rounded-[10px] border',
                      isSelected ? 'border-main' : 'border-line',
                    )}
                  >
                    <button
                      onClick={() => setTempSelectedCoupon(coupon)}
                      className="w-full flex text-left"
                    >
                      <div className="flex-1 px-[20px] py-[20px] flex flex-col">
                        <span className="text-[21px] leading-[21px] text-main">
                          {coupon.discountType === 'AMOUNT'
                            ? `${formatNumber(coupon.discountAmount)}p`
                            : `${coupon.discountAmount}%`}
                        </span>
                        <span className="mt-[15px] text-sm leading-[18px]">{coupon.name}</span>
                        <div className="flex flex-col gap-1 mt-2.5">
                          {coupon.minOrderAmount > 0 && (
                            <span className="text-xs leading-[14px] text-[#aaaaaa]">
                              {formatNumber(coupon.minOrderAmount)}원 이상 결제시
                            </span>
                          )}
                          {coupon.discountType === 'RATE' && coupon.maxDiscountAmount && (
                            <span className="text-xs leading-[14px] text-[#aaaaaa]">
                              최대 {formatNumber(coupon.maxDiscountAmount)}원 할인
                            </span>
                          )}
                          <span className="text-xs leading-[14px] text-[#aaaaaa]">
                            {coupon.useStartAt} ~ {coupon.useEndAt}
                          </span>
                        </div>
                      </div>
                      <div className="relative w-[120px] flex items-center justify-center">
                        <div
                          className={cn(
                            'absolute left-0 top-0 bottom-0 border-l border-dashed',
                            isSelected ? 'border-main' : 'border-line',
                          )}
                        />
                        <div className="absolute left-[-8px] top-[-8px] w-4 h-4 rounded-full bg-white" />
                        <div className="absolute left-[-8px] bottom-[-8px] w-4 h-4 rounded-full bg-white" />
                        <span className="text-base leading-[16px] text-[#666666]">
                          D-{coupon.daysRemaining}
                        </span>
                      </div>
                    </button>
                  </div>
                )
              })}
            </div>
            <div className="mt-5">
              <AppPrimaryButton onClick={handleApplyCoupon}>
                {getButtonText()}
              </AppPrimaryButton>
            </div>
          </div>
        </DrawerContent>
      </Drawer>
    </div>
  )
}
