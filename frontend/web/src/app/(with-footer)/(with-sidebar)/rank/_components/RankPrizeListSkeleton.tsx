import { Skeleton } from '@/components/ui/shadcn/skeleton'

function RankPrizeListItemSkeleton() {
  return (
    <div className="flex flex-col items-center flex-1 min-w-0">
      <div className="relative w-full max-w-[144px] mb-[15px] aspect-square">
        <Skeleton className="w-full h-full flex items-center justify-center border border-line rounded-full" />
      </div>
      <div className="flex flex-col items-center gap-1 w-full text-center">
        <Skeleton className="w-14 h-[11px]" />
        <Skeleton className="w-20 h-[11px]" />
      </div>
    </div>
  )
}

export function RankPrizeListSkeleton() {
  return (
    <>
      {[...Array(3)].map((_, i) => (
        <RankPrizeListItemSkeleton key={i} />
      ))}
    </>
  )
}
