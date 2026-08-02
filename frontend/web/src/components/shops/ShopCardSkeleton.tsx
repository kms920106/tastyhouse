import { Skeleton } from '@/components/ui/shadcn/skeleton'
import { cn } from '@/lib/utils'

export function ShopCardSkeleton({ className }: { className?: string }) {
  return (
    <div className={cn('group block overflow-hidden', className)}>
      <ShopCardImageSkeleton />
      <ShopCardContentSkeleton />
    </div>
  )
}

export function ShopCardImageSkeleton({ className }: { className?: string }) {
  return (
    <div className={cn('relative mb-[15px] aspect-square overflow-hidden', className)}>
      <Skeleton className="h-full w-full rounded-none" />
    </div>
  )
}

export function ShopCardContentSkeleton({ className }: { className?: string }) {
  return (
    <div className={className}>
      <div className="flex items-center justify-between mb-[9px]">
        <Skeleton className="w-1/4 h-3" />
        <Skeleton className="w-1/6 h-[17px]" />
      </div>
      <Skeleton className="h-4 w-3/4" />
      <div className="flex gap-[11px] mt-2.5">
        <Skeleton className="h-3 w-1/5" />
        <Skeleton className="h-3 w-1/5" />
      </div>
      <div className="flex gap-1.5 overflow-hidden mt-[15px]">
        <Skeleton className="w-16 h-[28px] rounded-[12.5px]" />
        <Skeleton className="w-16 h-[28px] rounded-[12.5px]" />
      </div>
    </div>
  )
}
