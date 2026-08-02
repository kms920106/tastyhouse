import { Skeleton } from '@/components/ui/shadcn/skeleton'

export default function PointHistoryItemSkeleton() {
  return (
    <div className="py-5">
      <div className="flex items-center justify-between">
        <div className="flex flex-col gap-2.5 flex-1">
          <Skeleton className="w-[100px] h-[14px]" />
          <Skeleton className="w-[80px] h-[11px]" />
        </div>
        <Skeleton className="w-[60px] h-[14px]" />
      </div>
    </div>
  )
}
