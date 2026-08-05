import { formatNumber } from '@/lib/number'
import { cn } from '@/lib/utils'
import * as React from 'react'

interface Props extends React.HTMLAttributes<HTMLParagraphElement> {
  /** 가게 최소주문금액. 0이면 미설정이라 렌더하지 않는다 */
  value: number
}

export function ShopCardMinOrder({ value, className, ...props }: Props) {
  if (value <= 0) {
    return null
  }

  return (
    <p
      className={cn('mt-2.5 text-xs leading-[12px] text-[#666666] tracking-tighter', className)}
      {...props}
    >
      최소주문 {formatNumber(value)}원
    </p>
  )
}
