import { ReviewImageGallerySkeleton } from '@/components/reviews/ReviewImageGallerySkeleton'
import { Skeleton } from '@/components/ui/shadcn/skeleton'

function ReviewPanelItemSkeleton() {
  return (
    <div className="py-5">
      <div className="flex justify-between">
        <div className="flex items-center gap-2.5">
          <Skeleton className="w-10 h-10 rounded-full flex-shrink-0" />
          <div className="flex flex-col gap-2">
            <Skeleton className="w-20 h-3.5" />
            <Skeleton className="w-16 h-3" />
          </div>
        </div>
        <Skeleton className="w-4 h-[19px]" />
      </div>
      <div className="mt-[25px]">
        <Skeleton className="w-32 h-[14px]" />
      </div>
      <div className="mt-[15px] space-y-2">
        <Skeleton className="w-full h-3.5" />
        <Skeleton className="w-full h-3.5" />
        <Skeleton className="w-3/4 h-3.5" />
      </div>
      <div className="mt-5">
        <ReviewImageGallerySkeleton />
      </div>
    </div>
  )
}

export default function ReviewPanelSkeleton() {
  return (
    <section className="flex flex-col gap-[3px] px-[15px] py-5">
      <div className="flex flex-col gap-[30px] pb-2.5 border-b border-line box-border">
        <div className="flex gap-2.5">
          {[1, 2, 3, 4, 5, 6].map((i) => (
            <Skeleton key={i} className="h-[42px] w-[70px] rounded-[1px]" />
          ))}
        </div>
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2">
            <Skeleton className="w-[25px] h-[25px] rounded-full" />
            <Skeleton className="w-24 h-[14px]" />
          </div>
          <div className="flex items-center gap-1.5">
            <Skeleton className="w-12 h-5" />
            <Skeleton className="w-5 h-5" />
          </div>
        </div>
      </div>
      <div className="flex flex-col divide-y divide-line">
        {[1, 2, 3].map((i) => (
          <ReviewPanelItemSkeleton key={i} />
        ))}
      </div>
    </section>
  )
}
