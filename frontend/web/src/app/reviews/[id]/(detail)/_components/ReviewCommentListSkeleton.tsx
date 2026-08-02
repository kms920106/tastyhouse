import { Skeleton } from '@/components/ui/shadcn/skeleton'

function ReviewCommentListItemSkeleton() {
  return (
    <div className="flex gap-2.5">
      <Skeleton className="w-9 h-9 rounded-full flex-shrink-0" />
      <div className="flex-1">
        <div className="flex items-center gap-[15px] mb-2.5">
          <Skeleton className="h-[14px] w-[60px]" />
          <Skeleton className="h-[12px] w-[40px]" />
        </div>
        <Skeleton className="h-[12px] w-full" />
        <Skeleton className="h-[12px] w-3/4 mt-1" />
      </div>
    </div>
  )
}

export function ReviewCommentListSkeleton() {
  return (
    <>
      {[...Array(5)].map((_, i) => (
        <ReviewCommentListItemSkeleton key={i} />
      ))}
    </>
  )
}
