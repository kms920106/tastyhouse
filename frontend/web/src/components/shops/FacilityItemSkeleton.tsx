import { Skeleton } from '@/components/ui/shadcn/skeleton'

export function FacilityItemSkeleton() {
  return (
    <div
      className="flex flex-col items-center justify-center px-5 py-[17px] border border-line box-border"
      style={{ aspectRatio: '80 / 95' }}
    >
      <Skeleton className="w-[38px] h-[38px] mb-[15px]" />
      <Skeleton className="w-12 h-3" />
    </div>
  )
}
