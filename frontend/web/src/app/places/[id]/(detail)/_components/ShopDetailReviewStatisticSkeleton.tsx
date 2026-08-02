import { ReviewRatingDetailSkeleton } from '@/components/reviews/ReviewRatingDetailSkeleton'
import { ReviewRatingScoreSkeleton } from '@/components/reviews/ReviewRatingScoreSkeleton'
import { Skeleton } from '@/components/ui/shadcn/skeleton'

export default function ShopDetailReviewStatisticSkeleton() {
  return (
    <>
      <div className="flex items-center justify-center gap-[30px] pt-[30px] pb-[21px] border-b border-line box-border">
        <ReviewRatingScoreSkeleton />
        <div className="flex items-end justify-center gap-[13px]">
          {[5, 4, 3, 2, 1].map((rating) => (
            <div key={rating} className="flex flex-col items-center gap-[13px]">
              <Skeleton className="w-[5px] h-[50px] rounded-full" />
              <Skeleton className="w-[18px] h-[12px]" />
            </div>
          ))}
        </div>
      </div>
      <div className="px-[15px] pt-[19px] pb-[30px]">
        <ReviewRatingDetailSkeleton />
      </div>
    </>
  )
}
