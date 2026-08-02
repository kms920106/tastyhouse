import { formatNumber } from '@/lib/number'

interface PointBalanceProps {
  availablePoints: number | null
  expiredThisMonth: number | null
}

export default function PointBalance({ availablePoints, expiredThisMonth }: PointBalanceProps) {
  return (
    <div className="py-[30px] text-center">
      <p className="text-xs leading-[12px]">사용 가능 포인트</p>
      <p className="mt-2.5 text-[23px] leading-[23px] text-main">
        {formatNumber(availablePoints ?? 0)} p
      </p>
      <p className="mt-[15px] text-xs leading-[12px] text-[#aaaaaa]">
        이번달 소멸 예정 포인트 <span className="text-[#666666]">{expiredThisMonth ?? 0}p</span>
      </p>
    </div>
  )
}
