import { cn } from '@/lib/utils'
import * as React from 'react'

interface Props extends React.HTMLAttributes<HTMLDivElement> {
  reviewCount: number
  bookmarkCount: number
}

export function ShopCardStats({ reviewCount, bookmarkCount, className, ...props }: Props) {
  return (
    <div className={cn('flex gap-[11px] mt-2.5', className)} {...props}>
      <p className="text-xs leading-[12px] text-[#666666] tracking-tighter">리뷰 {reviewCount}</p>
      <p className="text-xs leading-[12px] text-[#666666] tracking-tighter">찜 {bookmarkCount}</p>
    </div>
  )
}
