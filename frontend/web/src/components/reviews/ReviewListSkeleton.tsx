import { Skeleton } from '@/components/ui/shadcn/skeleton'

export default function ReviewListSkeleton() {
  return (
    <>
      <div className="py-[1px]">
        <div className="grid grid-cols-3 gap-[1.5px]">
          {Array.from({ length: 9 }).map((_, index) => (
            <Skeleton key={index} className="relative aspect-square" />
          ))}
        </div>
      </div>
      <div className="h-[70px]" />
    </>
  )
}
