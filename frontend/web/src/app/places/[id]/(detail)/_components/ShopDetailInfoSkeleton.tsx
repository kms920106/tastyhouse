import { FacilitySelectorSkeleton } from '@/components/shops/FacilitySelectorSkeleton'
import { Skeleton } from '@/components/ui/shadcn/skeleton'

export function ShopDetailInfoSkeleton() {
  return (
    <>
      <div className="relative mt-[13px] px-[15px] py-[23px] pb-4 bg-[#f9f9f9] border border-[#cccccc] box-border rounded-[5px]">
        <div className="absolute -top-3 left-[10px] inline-block px-3.5 py-[6.5px] mb-3 bg-main text-xs leading-[12px] text-white rounded-full">
          사장님 한마디
        </div>
        <Skeleton className="h-4 w-full" />
      </div>
      <div className="pt-[30px] pb-5 space-y-[15px] border-b border-line box-border">
        <div className="flex justify-between">
          <Skeleton className="h-[14px] w-16" />
          <div className="space-y-2">
            <Skeleton className="h-[14px] w-32" />
            <Skeleton className="h-[14px] w-32" />
          </div>
        </div>
        <div className="flex justify-between">
          <Skeleton className="h-[14px] w-20" />
          <div className="space-y-2">
            <Skeleton className="h-[14px] w-28" />
          </div>
        </div>
        <div className="flex justify-between">
          <Skeleton className="h-[14px] w-12" />
          <Skeleton className="h-[14px] w-24" />
        </div>
        <div className="flex justify-between">
          <Skeleton className="h-[14px] w-16" />
          <Skeleton className="h-[14px] w-32" />
        </div>
      </div>
      <div className="pt-5">
        <Skeleton className="h-[14px] w-16 mb-[15px]" />
        <FacilitySelectorSkeleton />
      </div>
    </>
  )
}
