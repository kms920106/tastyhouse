'use client'

import type { ScheduledOrderSlot } from '@/domains/shop'

interface Props {
  /** 선택된 슬롯. 미선택이면 null — 이때는 예약 진입 안내 문구를 노출한다 */
  selectedSlot: ScheduledOrderSlot | null
  onClick: () => void
}

/**
 * 장바구니 예약주문 진입 버튼.
 *
 * 미선택이면 "예약 주문", 선택했으면 서버가 내려준 `dayLabel` + `label`을 그대로 보여줍니다.
 * 문구를 프론트에서 조립하지 않는 것은 배달팁 breakdown과 같은 원칙입니다.
 */
export default function ScheduledOrderTrigger({ selectedSlot, onClick }: Props) {
  return (
    <button
      type="button"
      onClick={onClick}
      className="w-full px-[15px] py-4 flex items-center justify-between gap-2 cursor-pointer"
    >
      <span className="flex items-center gap-1.5 text-sm leading-[14px]">
        <span aria-hidden>🕐</span>
        {selectedSlot ? (
          <span>
            {selectedSlot.dayLabel} {selectedSlot.label} 예약
          </span>
        ) : (
          <span>예약 주문</span>
        )}
      </span>
      <span aria-hidden className="text-sm leading-[14px] text-[#aaaaaa]">
        &gt;
      </span>
    </button>
  )
}
