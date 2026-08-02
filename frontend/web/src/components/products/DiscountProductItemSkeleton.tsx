import { Skeleton } from '@/components/ui/shadcn/skeleton'

export function DiscountProductItemSkeleton() {
  return (
    <div className="flex items-center gap-4">
      <div className="relative w-[75px] h-[75px] flex-shrink-0 overflow-hidden rounded-none">
        <Skeleton className="h-[75px] w-[75px] rounded-none" />
      </div>
      <div className="flex-1 min-w-0 h-[75px] flex flex-col">
        <Skeleton className="h-3 w-24 mb-3" />
        <Skeleton className="h-4 w-48 mb-auto" />
        <div className="flex justify-between mt-auto">
          <div className="flex items-end gap-2">
            <Skeleton className="h-4 w-20" />
            <Skeleton className="h-3 w-16" />
          </div>
          <div className="text-right flex-shrink-0">
            <Skeleton className="h-4 w-12" />
          </div>
        </div>
      </div>
    </div>
  )
}
