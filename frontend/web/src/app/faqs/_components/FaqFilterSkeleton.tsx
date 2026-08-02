import { Skeleton } from '@/components/ui/shadcn/skeleton'

export function FaqFilterSkeleton() {
  return (
    <div className="pl-[15px] py-5 flex gap-[10px]">
      {Array.from({ length: 10 }).map((_, i) => (
        <Skeleton key={i} className="h-[42px] w-[60px] flex-shrink-0" />
      ))}
    </div>
  )
}
