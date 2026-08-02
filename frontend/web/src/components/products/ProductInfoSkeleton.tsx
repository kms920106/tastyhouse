import { Skeleton } from '@/components/ui/shadcn/skeleton'

export function ProductInfoSkeleton() {
  return (
    <div className="px-[15px] py-[21px]">
      <Skeleton className="h-[18px] w-1/2" />
      <Skeleton className="mt-[13px] h-[14px] w-full" />
      <Skeleton className="mt-2 h-[14px] w-4/5" />
      <Skeleton className="mt-[17px] h-[16px] w-1/4" />
    </div>
  )
}
