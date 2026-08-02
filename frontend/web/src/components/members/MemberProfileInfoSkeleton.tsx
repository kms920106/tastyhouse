import { Skeleton } from '@/components/ui/shadcn/skeleton'
import { ReactNode } from 'react'

export function MemberProfileInfoSkeleton({ editSlot }: { editSlot?: ReactNode }) {
  return (
    <>
      <div className="-mt-[63px] relative z-10">
        <Skeleton className="w-[125px] h-[125px] rounded-full" />
      </div>
      <div className="flex items-center gap-0.5 mt-[21px]">
        <Skeleton className="h-[16px] w-[80px]" />
        {editSlot}
      </div>
      <div className="flex items-center gap-1.5 mt-2">
        <Skeleton className="w-[14px] h-[14px] rounded-full" />
        <Skeleton className="h-[14px] w-[50px]" />
      </div>
      <Skeleton className="h-[14px] w-[160px] mt-[15px]" />
    </>
  )
}
