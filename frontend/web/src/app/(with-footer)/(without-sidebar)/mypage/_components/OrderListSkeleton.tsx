import { Skeleton } from '@/components/ui/shadcn/skeleton'

function OrderListItemSkeleton() {
  return (
    <div className="flex items-center justify-between py-[15px]">
      <div className="flex items-center gap-[15px]">
        <Skeleton className="w-[60px] h-[60px] rounded-md" />
        <div className="flex flex-col">
          <Skeleton className="h-[11px] w-[60px]" />
          <Skeleton className="h-[14px] w-[120px] mt-[7px]" />
          <Skeleton className="h-[14px] w-[80px] mt-2.5" />
        </div>
      </div>
      <div className="flex flex-col items-end gap-[7px]">
        <Skeleton className="h-[11px] w-[55px]" />
        <Skeleton className="h-[11px] w-[45px]" />
      </div>
    </div>
  )
}

export function OrderListSkeleton() {
  return (
    <div className="px-[15px] py-[5px] bg-white divide-y divide-line">
      {Array.from({ length: 3 }).map((_, index) => (
        <OrderListItemSkeleton key={index} />
      ))}
    </div>
  )
}
