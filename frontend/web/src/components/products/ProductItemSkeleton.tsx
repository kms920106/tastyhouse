import { Skeleton } from '@/components/ui/shadcn/skeleton'

export function ProductItemSkeleton() {
  return (
    <div className="flex items-center gap-[15px] pr-3">
      <Skeleton className="w-[65px] h-[65px] flex-shrink-0" />
      <div className="flex-1 flex flex-col min-w-0">
        <div className="flex items-center justify-between">
          <div className="flex flex-col">
            <Skeleton className="w-9 h-[15px] mb-[7px]" />
            <Skeleton className="w-32 h-[14px] mb-[9px]" />
            <Skeleton className="w-20 h-[14px]" />
          </div>
          <div className="flex flex-col items-center gap-2.5">
            <Skeleton className="w-8 h-[19px]" />
            <Skeleton className="w-12 h-[12px]" />
          </div>
        </div>
      </div>
    </div>
  )
}
