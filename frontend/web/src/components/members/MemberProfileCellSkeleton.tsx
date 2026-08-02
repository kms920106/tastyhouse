import { Skeleton } from '@/components/ui/shadcn/skeleton'

export function MemberProfileCellSkeleton() {
  return (
    <div className="flex items-center gap-3">
      <Skeleton className="w-10 h-10 rounded-full" />
      <div className="flex flex-col gap-[9px]">
        <Skeleton className="w-24 h-3.5" />
        <Skeleton className="w-16 h-4 rounded-full" />
      </div>
    </div>
  )
}
