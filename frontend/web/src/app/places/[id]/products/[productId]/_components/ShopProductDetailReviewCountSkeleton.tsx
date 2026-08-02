import { Skeleton } from '@/components/ui/shadcn/skeleton'

export function ShopProductDetailReviewCountSkeleton() {
  return (
    <div>
      <div className="flex w-full h-[50px] border-b border-line">
        <div className="flex-1 flex items-center justify-center">
          <Skeleton className="h-[14px] w-16" />
        </div>
      </div>
      <div className="px-4 py-5">
        <Skeleton className="h-[16px] w-1/3" />
        <div className="flex flex-col gap-[15px] mt-5">
          <Skeleton className="h-[14px] w-full" />
          <Skeleton className="h-[14px] w-full" />
          <Skeleton className="h-[14px] w-4/5" />
        </div>
      </div>
    </div>
  )
}
