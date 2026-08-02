import { MemberProfileCellSkeleton } from '@/components/members/MemberProfileCellSkeleton'
import { Skeleton } from '@/components/ui/shadcn/skeleton'

function MemberFollowListItemSkeleton() {
  return (
    <div className="flex items-center justify-between">
      <MemberProfileCellSkeleton />
      <div className="flex items-center gap-2">
        <Skeleton className="w-[73px] h-[31px] rounded-[2.5px]" />
        <Skeleton className="w-8 h-8 rounded" />
      </div>
    </div>
  )
}

export function MemberFollowListSkeleton() {
  return (
    <div className="flex flex-col gap-[30px] py-[30px]">
      {[...Array(10)].map((_, i) => (
        <MemberFollowListItemSkeleton key={i} />
      ))}
    </div>
  )
}
