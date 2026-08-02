import { Skeleton } from '@/components/ui/shadcn/skeleton'

export function ReviewCommentInputSkeleton() {
  return (
    <div className="flex items-center gap-[7px] flex-1">
      <Skeleton className="size-10 rounded-full" />
      <Skeleton className="flex-1 h-10 rounded-[20px]" />
    </div>
  )
}
