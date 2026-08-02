import { Skeleton } from '@/components/ui/shadcn/skeleton'

export default function ReservationCompleteSkeleton() {
  return (
    <>
      <div className="flex-1 flex flex-col items-center justify-center gap-[30px] px-[15px]">
        <Skeleton className="w-[95px] h-[95px] rounded-full" />
        <div className="flex flex-col items-center gap-[15px]">
          <Skeleton className="w-[200px] h-[23px]" />
          <Skeleton className="w-[240px] h-[14px]" />
        </div>
        <div className="w-full rounded-xl border border-line p-[20px]">
          <div className="flex items-center gap-[12px]">
            <Skeleton className="w-[60px] h-[60px] rounded-lg shrink-0" />
            <div className="flex flex-col gap-[8px]">
              <Skeleton className="w-[120px] h-[16px]" />
              <Skeleton className="w-[200px] h-[14px]" />
            </div>
          </div>
        </div>
      </div>
      <div className="px-[15px] py-2.5">
        <Skeleton className="w-full h-[52px] rounded-lg" />
      </div>
    </>
  )
}
