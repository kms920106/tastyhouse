import { MemberProfileCellSkeleton } from '@/components/members/MemberProfileCellSkeleton'
import { Skeleton } from '@/components/ui/shadcn/skeleton'

function MemberSearchResultItemSkeleton() {
  return (
    <div className="flex items-center justify-between">
      <MemberProfileCellSkeleton />
      <Skeleton className="w-[73px] h-[31px] rounded-[2.5px]" />
    </div>
  )
}

export function MemberSearchResultListSkeleton() {
  return (
    <div className="flex flex-col gap-[30px] py-[30px]">
      {[...Array(5)].map((_, i) => (
        <MemberSearchResultItemSkeleton key={i} />
      ))}
    </div>
  )
}
