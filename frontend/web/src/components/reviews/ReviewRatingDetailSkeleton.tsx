import { Skeleton } from '@/components/ui/shadcn/skeleton'

export function ReviewRatingDetailSkeleton() {
  return (
    <div className="flex flex-row justify-between">
      <div className="space-y-2.5">
        <div className="flex-1 flex items-center">
          <div className="flex items-center gap-5 w-full">
            <Skeleton className="w-14 h-3" />
            <div className="flex-1 flex items-center gap-[7px]">
              <div className="flex items-center">
                {[1, 2, 3, 4, 5].map((star) => (
                  <Skeleton key={star} className="w-[14px] h-[14px]" />
                ))}
              </div>
              <Skeleton className="h-3 w-8" />
            </div>
          </div>
        </div>
        <div className="flex-1 flex items-center">
          <div className="flex items-center gap-5 w-full">
            <Skeleton className="w-14 h-3" />
            <div className="flex-1 flex items-center gap-[7px]">
              <div className="flex items-center">
                {[1, 2, 3, 4, 5].map((star) => (
                  <Skeleton key={star} className="w-[14px] h-[14px]" />
                ))}
              </div>
              <Skeleton className="h-3 w-8" />
            </div>
          </div>
        </div>
        <div className="flex-1 flex items-center">
          <div className="flex items-center gap-5 w-full">
            <Skeleton className="w-14 h-3" />
            <div className="flex-1 flex items-center gap-[7px]">
              <div className="flex items-center">
                {[1, 2, 3, 4, 5].map((star) => (
                  <Skeleton key={star} className="w-[14px] h-[14px]" />
                ))}
              </div>
              <Skeleton className="h-3 w-8" />
            </div>
          </div>
        </div>
        <div className="flex-1 flex items-center">
          <div className="flex items-center gap-5 w-full">
            <Skeleton className="w-14 h-3" />
            <Skeleton className="w-18 h-3" />
          </div>
        </div>
      </div>
      <div className="space-y-2.5">
        <div className="flex-1 flex items-center">
          <div className="flex items-center gap-5 w-full">
            <Skeleton className="w-14 h-3" />
            <div className="flex-1 flex items-center gap-[7px]">
              <div className="flex items-center">
                {[1, 2, 3, 4, 5].map((star) => (
                  <Skeleton key={star} className="w-[14px] h-[14px]" />
                ))}
              </div>
              <Skeleton className="h-3 w-8" />
            </div>
          </div>
        </div>
        <div className="flex-1 flex items-center">
          <div className="flex items-center gap-5 w-full">
            <Skeleton className="w-14 h-3" />
            <div className="flex-1 flex items-center gap-[7px]">
              <div className="flex items-center">
                {[1, 2, 3, 4, 5].map((star) => (
                  <Skeleton key={star} className="w-[14px] h-[14px]" />
                ))}
              </div>
              <Skeleton className="h-3 w-8" />
            </div>
          </div>
        </div>
        <div className="flex-1 flex items-center">
          <div className="flex items-center gap-5 w-full">
            <Skeleton className="w-14 h-3" />
            <div className="flex-1 flex items-center gap-[7px]">
              <div className="flex items-center">
                {[1, 2, 3, 4, 5].map((star) => (
                  <Skeleton key={star} className="w-[14px] h-[14px]" />
                ))}
              </div>
              <Skeleton className="h-3 w-8" />
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}
