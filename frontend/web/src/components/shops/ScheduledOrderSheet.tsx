'use client'

import AppPrimaryButton from '@/components/ui/AppPrimaryButton'
import {
  Drawer,
  DrawerContent,
  DrawerDescription,
  DrawerFooter,
  DrawerHeader,
  DrawerTitle,
} from '@/components/ui/shadcn/drawer'
import { COMMON_ERROR_MESSAGES } from '@/constants/errors'
import type { OrderMethodType } from '@/domains/order'
import type { ScheduledOrderSlot } from '@/domains/shop'
import { useScheduledOrderSlots } from '@/domains/shop/shop.hook'
import { cn } from '@/lib/utils'
import { useEffect, useMemo, useState } from 'react'

const EMPTY_MESSAGE = '지금은 예약 가능한 시간이 없어요'

interface Props {
  open: boolean
  onOpenChange: (open: boolean) => void
  shopId: number
  orderMethod: OrderMethodType
  /** 현재 선택된 슬롯 시작 시각. 미선택이면 null */
  selectedStartAt: string | null
  /** 선택 완료 시 호출. 선택을 해제하면 null이 전달된다 */
  onSelect: (slot: ScheduledOrderSlot | null) => void
}

/**
 * 수령시간 예약 바텀시트.
 *
 * 장바구니와 결제하기 두 화면에서 재사용합니다. 슬롯은 시간이 지나면 사라지므로 열릴 때마다
 * 새로 조회하며(캐시 없음), 이미 고른 시각이 목록에서 사라졌으면 선택을 해제합니다.
 */
export default function ScheduledOrderSheet({
  open,
  onOpenChange,
  shopId,
  orderMethod,
  selectedStartAt,
  onSelect,
}: Props) {
  const { availability, isLoading, isError } = useScheduledOrderSlots(shopId, {
    orderMethod,
    enabled: open,
  })

  const [draftStartAt, setDraftStartAt] = useState<string | null>(selectedStartAt)

  const slots = useMemo(() => availability?.slots ?? [], [availability])

  // 시트를 열 때 현재 확정값으로 되돌린다 — 이전에 고르다 만 값이 남아 있으면 안 된다.
  useEffect(() => {
    if (open) setDraftStartAt(selectedStartAt)
  }, [open, selectedStartAt])

  // 고른 슬롯이 재조회 목록에서 사라졌으면(리드타임 경과 등) 선택을 해제한다.
  useEffect(() => {
    if (!open || isLoading || draftStartAt === null) return
    if (!slots.some((slot) => slot.startAt === draftStartAt)) {
      setDraftStartAt(null)
    }
  }, [open, isLoading, draftStartAt, slots])

  const leadTimeGuide = availability
    ? `주문 후 ${formatLeadTime(availability.leadTimeMinutes)} 이후부터 예약할 수 있어요`
    : null

  const handleConfirm = () => {
    const selected = slots.find((slot) => slot.startAt === draftStartAt) ?? null
    onSelect(selected)
    onOpenChange(false)
  }

  const isEmpty = availability !== null && (!availability.available || slots.length === 0)

  return (
    <Drawer open={open} onOpenChange={onOpenChange}>
      <DrawerContent>
        <DrawerHeader className="px-[15px] pt-5 pb-2.5">
          <DrawerTitle className="text-base leading-[16px] font-bold">수령시간 선택</DrawerTitle>
          <DrawerDescription className="text-xs leading-[16px] text-[#666666]">
            {leadTimeGuide ?? '예약 가능한 시간을 확인하고 있어요'}
          </DrawerDescription>
        </DrawerHeader>

        <div className="px-[15px] pb-2.5 max-h-[50vh] overflow-y-auto">
          {isLoading && (
            <p className="py-10 text-sm leading-[14px] text-[#666666] text-center">
              불러오는 중입니다.
            </p>
          )}
          {!isLoading && isError && (
            <p className="py-10 text-sm leading-[14px] text-[#666666] text-center">
              {COMMON_ERROR_MESSAGES.API_FETCH_ERROR}
            </p>
          )}
          {!isLoading && !isError && isEmpty && (
            <p className="py-10 text-sm leading-[14px] text-[#666666] text-center">
              {EMPTY_MESSAGE}
            </p>
          )}
          {!isLoading && !isError && !isEmpty && (
            <ul className="flex flex-col gap-2">
              {slots.map((slot) => (
                <li key={slot.startAt}>
                  <button
                    type="button"
                    onClick={() => setDraftStartAt(slot.startAt)}
                    aria-pressed={draftStartAt === slot.startAt}
                    className={cn(
                      'w-full px-[15px] py-3 flex items-center gap-2 text-left border border-line cursor-pointer',
                      draftStartAt === slot.startAt && 'border-main',
                    )}
                  >
                    <span className="text-xs leading-[12px] text-[#aaaaaa]">{slot.dayLabel}</span>
                    <span className="text-sm leading-[14px]">{slot.label}</span>
                  </button>
                </li>
              ))}
            </ul>
          )}
        </div>

        <DrawerFooter className="px-[15px] pb-5">
          <AppPrimaryButton onClick={handleConfirm} disabled={isLoading}>
            선택 완료
          </AppPrimaryButton>
        </DrawerFooter>
      </DrawerContent>
    </Drawer>
  )
}

/** 리드타임(분)을 안내 문구용 한국어로 바꾼다. 30분 단위 슬롯이라 분 단위는 나오지 않는다. */
function formatLeadTime(leadTimeMinutes: number): string {
  if (leadTimeMinutes < 60) return `${leadTimeMinutes}분`

  const hours = Math.floor(leadTimeMinutes / 60)
  const minutes = leadTimeMinutes % 60
  return minutes === 0 ? `${hours}시간` : `${hours}시간 ${minutes}분`
}
