import { Skeleton } from '@/components/ui/shadcn/skeleton'

export default function ReviewThumbnailSkeleton() {
  return (
    <div className="relative aspect-square overflow-hidden">
      <Skeleton className="absolute inset-0 rounded-none" />
    </div>
  )
}
