import { Skeleton } from '@/components/ui/shadcn/skeleton'

export default function SearchPopularKeywordListItemSkeleton() {
  return (
    <div className="flex items-center gap-5">
      <Skeleton className="w-5 h-5 shrink-0" />
      <Skeleton className="flex-1 h-5" />
    </div>
  )
}
