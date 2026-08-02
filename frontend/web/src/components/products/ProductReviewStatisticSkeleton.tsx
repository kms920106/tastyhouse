import { ReviewRatingScoreSkeleton } from '@/components/reviews/ReviewRatingScoreSkeleton'
import { Skeleton } from '@/components/ui/shadcn/skeleton'

export function ProductReviewStatisticSkeleton() {
  return (
    <div className="flex items-center justify-center gap-[50px] pt-[30px] pb-[21px]">
      <ReviewRatingScoreSkeleton />
      <div className="flex flex-col gap-2.5">
        <Skeleton className="w-20 h-3" />
        <Skeleton className="w-20 h-3" />
        <Skeleton className="w-20 h-3" />
      </div>
    </div>
  )
}
