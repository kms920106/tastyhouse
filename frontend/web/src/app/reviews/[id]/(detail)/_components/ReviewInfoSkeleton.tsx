import { Skeleton } from '@/components/ui/shadcn/skeleton'

export function ReviewInfoSkeleton() {
  return (
    <>
      <div className="flex justify-between mb-[15px]">
        <div className="flex items-center gap-2.5">
          <Skeleton className="w-9 h-9 rounded-full" />
          <div className="flex flex-col gap-2">
            <Skeleton className="h-[14px] w-[80px]" />
            <Skeleton className="h-[12px] w-[50px]" />
          </div>
        </div>
      </div>
      <Skeleton className="aspect-[345/190] w-full mb-5 rounded-none" />
      <div className="space-y-2">
        <Skeleton className="h-[14px] w-full" />
        <Skeleton className="h-[14px] w-full" />
        <Skeleton className="h-[14px] w-3/4" />
      </div>
      <div className="flex flex-wrap gap-[7px] mt-5">
        <Skeleton className="h-[26px] w-[60px] rounded-full" />
        <Skeleton className="h-[26px] w-[80px] rounded-full" />
        <Skeleton className="h-[26px] w-[70px] rounded-full" />
      </div>
      <div className="flex items-center gap-5 mt-[15px] pt-[17px] border-t border-line">
        <Skeleton className="h-[12px] w-[60px]" />
        <Skeleton className="h-[12px] w-[50px]" />
      </div>
    </>
  )
}
