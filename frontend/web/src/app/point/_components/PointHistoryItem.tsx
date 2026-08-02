import { PointType } from '@/domains/member/member.types'
import { formatNumber } from '@/lib/number'
import { cn } from '@/lib/utils'

interface PointHistoryItemProps {
  reason: string
  date: string
  pointType: PointType
  pointAmount: number
}

export default function PointHistoryItem({
  reason,
  date,
  pointType,
  pointAmount,
}: PointHistoryItemProps) {
  return (
    <div className="py-5">
      <div className="flex items-center justify-between">
        <div className="flex flex-col gap-2.5">
          <p className="text-sm leading-[14px] text-gray-900">{reason}</p>
          <p className="text-[11px] leading-[11px] text-[#aaaaaa]">{date}</p>
        </div>
        <p
          className={cn(
            'text-sm leading-[14px]',
            pointType === 'EARNED' ? 'text-main' : 'text-gray-900',
          )}
        >
          {pointType === 'EARNED' ? '+' : '-'}
          {formatNumber(pointAmount)} p
        </p>
      </div>
    </div>
  )
}
