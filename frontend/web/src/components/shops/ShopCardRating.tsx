import { formatDecimal } from '@/lib/number'
import { cn } from '@/lib/utils'
import * as React from 'react'

interface Props extends React.HTMLAttributes<HTMLSpanElement> {
  value: number
}

export function ShopCardRating({ value, className, ...props }: Props) {
  return (
    <span className={cn('text-[17px] leading-[17px] text-main', className)} {...props}>
      {formatDecimal(value, 1)}
    </span>
  )
}
