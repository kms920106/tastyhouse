import { Skeleton } from '@/components/ui/shadcn/skeleton'

export function ShopDetailSummarySkeleton() {
  return (
    <>
      <div className="flex items-start justify-between mb-6">
        <Skeleton className="w-1/4 h-[18px]" />
        <Skeleton className="w-10 h-[19px]" />
      </div>
      <div className="flex justify-between gap-3">
        <div className="flex-1 flex flex-col min-w-0 gap-[7px]">
          <Skeleton className="w-3/5 h-[14px]" />
          <Skeleton className=" w-2/5 h-[12px]" />
        </div>
      </div>
    </>
  )
}
