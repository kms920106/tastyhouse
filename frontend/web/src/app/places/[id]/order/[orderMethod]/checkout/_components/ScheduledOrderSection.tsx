'use client'

import type { ScheduledOrderSlot } from '@/domains/shop'

interface Props {
  /** 선택된 슬롯. 미선택이면 즉시 주문으로 안내한다 */
  selectedSlot: ScheduledOrderSlot | null
  onChangeClick: () => void
}

/**
 * 결제하기 수령시간 섹션.
 *
 * 장바구니에서 넘어온 예약 시각을 보여주고, `변경`으로 같은 바텀시트를 다시 엽니다.
 * 예약하지 않았으면 즉시 주문임을 명시합니다.
 */
export default function ScheduledOrderSection({ selectedSlot, onChangeClick }: Props) {
  return (
    <div className="px-[15px] py-5 flex items-center justify-between gap-3">
      <div className="flex flex-col gap-1.5">
        <h2 className="text-base leading-[16px]">수령시간</h2>
        <p className="text-sm leading-[14px] text-[#666666]">
          {selectedSlot ? `${selectedSlot.dayLabel} ${selectedSlot.label}` : '지금 바로 받기'}
        </p>
      </div>
      <button
        type="button"
        onClick={onChangeClick}
        className="shrink-0 px-3 py-2 text-sm leading-[14px] text-main border border-line cursor-pointer"
      >
        변경
      </button>
    </div>
  )
}
