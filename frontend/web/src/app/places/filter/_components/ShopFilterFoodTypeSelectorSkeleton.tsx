import { Skeleton } from '@/components/ui/shadcn/skeleton'

export function ShopFilterFoodTypeSelectorSkeleton() {
  return (
    <div className="grid grid-cols-4 gap-2.5">
      {Array.from({ length: 8 }).map((_, i) => (
        <div
          key={i}
          className="flex flex-col items-center justify-center border border-line px-5 py-[17px]"
          style={{ aspectRatio: '80 / 95' }}
        >
          <Skeleton className="w-[38px] h-[38px] mb-[15px]" />
          <Skeleton className="w-12 h-3" />
        </div>
      ))}
    </div>
  )
}
