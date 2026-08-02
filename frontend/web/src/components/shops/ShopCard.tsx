import { PAGE_PATHS } from '@/lib/paths'
import { cn } from '@/lib/utils'
import Link from 'next/link'
import * as React from 'react'

interface Props {
  shopId: number
  children: React.ReactNode
  className?: string
}

export function ShopCard({ shopId, children, className }: Props) {
  return (
    <Link
      href={PAGE_PATHS.PLACE_DETAIL(shopId)}
      className={cn('group block overflow-hidden', className)}
    >
      {children}
    </Link>
  )
}

export function ShopCardContent({
  children,
  className,
  ...props
}: React.HTMLAttributes<HTMLDivElement>) {
  return (
    <div className={className} {...props}>
      {children}
    </div>
  )
}

export function ShopCardHeader({
  children,
  className,
  ...props
}: React.HTMLAttributes<HTMLDivElement>) {
  return (
    <div className={cn('flex items-center justify-between mb-[9px]', className)} {...props}>
      {children}
    </div>
  )
}

export function ShopCardStation({
  children,
  className,
  ...props
}: React.HTMLAttributes<HTMLSpanElement>) {
  return (
    <span className={cn('text-xs leading-[12px] text-[#999999] truncate', className)} {...props}>
      {children}
    </span>
  )
}

export function ShopCardName({
  children,
  className,
  ...props
}: React.HTMLAttributes<HTMLHeadingElement>) {
  return (
    <h3 className={cn('text-base leading-[16px] truncate', className)} {...props}>
      {children}
    </h3>
  )
}

export { ShopCardImage } from './ShopCardImage'
export { ShopCardRating } from './ShopCardRating'
export { ShopCardStats } from './ShopCardStats'
export { ShopCardTags } from './ShopCardTags'
