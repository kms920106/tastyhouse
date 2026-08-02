import { Skeleton } from '@/components/ui/shadcn/skeleton'

export function RankScheduleSkeleton() {
  return (
    <div className="flex flex-col gap-1.5">
      <Skeleton className="w-40 h-3.5 ml-auto" />
      <Skeleton className="w-30 h-3.5 ml-auto" />
    </div>
  )
}
