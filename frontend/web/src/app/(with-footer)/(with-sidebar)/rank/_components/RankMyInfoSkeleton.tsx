import { Skeleton } from '@/components/ui/shadcn/skeleton'

export function RankMyInfoSkeleton() {
  return (
    <>
      <div className="flex gap-2.5">
        <div className="flex items-center flex-shrink-0 w-[22px]">
          <Skeleton className="w-4 h-3" />
        </div>
        <div className="flex-shrink-0">
          <Skeleton className="w-10 h-10 rounded-full" />
        </div>
        <div className="flex flex-col gap-1 min-w-0">
          <Skeleton className="w-30 h-5" />
          <div className="flex items-center gap-[5px]">
            <Skeleton className="w-5 h-4" />
            <Skeleton className="w-15 h-3" />
          </div>
        </div>
      </div>
      <Skeleton className="w-10 h-3" />
    </>
  )
}
