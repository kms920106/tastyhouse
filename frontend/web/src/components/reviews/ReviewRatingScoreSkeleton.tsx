import { Skeleton } from '@/components/ui/shadcn/skeleton'

export function ReviewRatingScoreSkeleton() {
  return (
    <div className="flex flex-col items-center">
      <div className="flex items-baseline gap-1 mb-[15px]">
        <Skeleton className="w-10 h-9" />
        <Skeleton className="w-[8px] h-[16px]" />
        <Skeleton className="w-[12px] h-[16px]" />
      </div>
      <div className="flex items-center gap-0 mb-2">
        {[1, 2, 3, 4, 5].map((star) => (
          <Skeleton key={star} className="w-[14px] h-[14px]" />
        ))}
      </div>
      <Skeleton className="w-16 h-[10px]" />
    </div>
  )
}
