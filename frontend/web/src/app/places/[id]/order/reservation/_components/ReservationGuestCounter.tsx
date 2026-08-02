'use client'

import QuantityStepper from '@/components/ui/QuantityStepper'

interface Props {
  count: number
  onChange: (next: number) => void
  max?: number
}

export default function ReservationGuestCounter({ count, onChange, max = 50 }: Props) {
  return (
    <div className="px-[15px] py-6">
      <h2 className="mb-4 text-base leading-[16px]">인원</h2>
      <div className="flex items-center justify-between">
        <p className="text-sm leading-[14px]">방문하시는 인원을 선택해주세요.</p>
        <QuantityStepper
          value={count}
          onChange={onChange}
          max={max}
          decrementLabel="인원 감소"
          incrementLabel="인원 증가"
        />
      </div>
    </div>
  )
}
