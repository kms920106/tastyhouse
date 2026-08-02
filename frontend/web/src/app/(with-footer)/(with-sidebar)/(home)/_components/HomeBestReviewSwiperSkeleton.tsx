import { Skeleton } from '@/components/ui/shadcn/skeleton'
import { cn } from '@/lib/utils'

function ReviewCardSkeleton({ className }: { className?: string }) {
  return (
    <div
      className={cn('flex-shrink-0 transition-all duration-300', className)}
      style={{ width: 'calc(100% / 1.5)' }}
    >
      <Skeleton className="relative w-full mb-[15px] pt-[75%] rounded-none" />
      <div className="mb-1.5 flex items-center justify-between">
        <Skeleton className="h-3 w-12" />
        <Skeleton className="h-5 w-8" />
      </div>
      <Skeleton className="mb-[19px] h-4 w-2/3" />
      <div className="space-y-1.5">
        <Skeleton className="h-3 w-full" />
        <Skeleton className="h-3 w-full" />
        <Skeleton className="h-3 w-4/5" />
      </div>
    </div>
  )
}

export function HomeBestReviewSwiperSkeleton() {
  return (
    <div className="overflow-hidden pb-[53px]">
      <div
        className="flex gap-4"
        style={{
          transform: 'translateX(calc(50% - (100% / 1.5 / 2) - 8px - (100% / 1.5) - 16px))',
        }}
      >
        <ReviewCardSkeleton className="scale-90 opacity-60" />
        <ReviewCardSkeleton />
        <ReviewCardSkeleton className="scale-90 opacity-60" />
      </div>
    </div>
  )
}
