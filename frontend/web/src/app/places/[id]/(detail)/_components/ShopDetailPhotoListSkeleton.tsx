import { ReviewImageGallerySkeleton } from '@/components/reviews/ReviewImageGallerySkeleton'
import { Skeleton } from '@/components/ui/shadcn/skeleton'

export function ShopDetailPhotoListSkeleton() {
  return (
    <>
      {Array.from({ length: 3 }).map((_, i) => (
        <div key={i} className="pt-[30px]">
          <Skeleton className="mb-[15px] w-8 h-[14px]" />
          <ReviewImageGallerySkeleton />
        </div>
      ))}
    </>
  )
}
