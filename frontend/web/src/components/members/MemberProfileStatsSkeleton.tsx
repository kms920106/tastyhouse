import { Skeleton } from '@/components/ui/shadcn/skeleton'
import { ReactNode } from 'react'

function StatLabel({ children }: { children: ReactNode }) {
  return <span className="text-xs leading-[12px]">{children}</span>
}

export function MemberProfileStatsSkeleton() {
  return (
    <div className="flex items-center justify-center gap-10 mt-[53px] mb-[30px]">
      <div className="flex items-center gap-1">
        <StatLabel>리뷰</StatLabel>
        <Skeleton className="h-[12px] w-[16px]" />
      </div>
      <div className="flex items-center gap-1">
        <StatLabel>팔로잉</StatLabel>
        <Skeleton className="h-[12px] w-[16px]" />
      </div>
      <div className="flex items-center gap-1">
        <StatLabel>팔로워</StatLabel>
        <Skeleton className="h-[12px] w-[16px]" />
      </div>
    </div>
  )
}
