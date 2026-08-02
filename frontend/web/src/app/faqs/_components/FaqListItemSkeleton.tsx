import { Skeleton } from '@/components/ui/shadcn/skeleton'

export default function FaqListItemSkeleton() {
  return (
    <div className="flex flex-col">
      {Array.from({ length: 10 }).map((_, i) => (
        <div key={i} className="px-[19px] py-[18px] border-b border-line">
          <div className="flex items-center gap-5">
            <Skeleton className="h-[14px] w-[40px] flex-shrink-0" />
            <Skeleton className="h-[14px] w-[200px]" />
          </div>
        </div>
      ))}
    </div>
  )
}
