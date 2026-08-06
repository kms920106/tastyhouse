import { formatNumber } from '@/lib/number'
import { cn } from '@/lib/utils'
import * as React from 'react'

interface Props extends React.HTMLAttributes<HTMLParagraphElement> {
  /** 배달팁 하한. min과 max가 모두 0이면 배달팁이 없어 렌더하지 않는다 */
  min: number
  /** 배달팁 상한 (고객 주소 확정 전) */
  max: number
}

/**
 * 가게 카드·목록의 배달팁 표기.
 *
 * 하한과 상한이 같으면 단일 금액(`배달팁 2,000원`), 다르면 범위(`배달팁 2,000~4,000원`)로 표기합니다.
 * 목록 API가 내려주는 min/max는 현재 시각·실제 거리가 아니라 설정값 전체의 하한/상한입니다.
 */
export function ShopCardDeliveryTip({ min, max, className, ...props }: Props) {
  if (max <= 0) {
    return null
  }

  const label =
    min === max ? `${formatNumber(min)}원` : `${formatNumber(min)}~${formatNumber(max)}원`

  return (
    <p
      className={cn('mt-2.5 text-xs leading-[12px] text-[#666666] tracking-tighter', className)}
      {...props}
    >
      배달팁 {label}
    </p>
  )
}
