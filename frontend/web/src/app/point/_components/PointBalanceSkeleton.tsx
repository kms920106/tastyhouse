import { Skeleton } from '@/components/ui/shadcn/skeleton'

export default function PointBalanceSkeleton() {
  return (
    <div className="py-[30px] text-center">
      <p className="text-xs leading-[12px]">사용 가능 포인트</p>
      <p className="mt-2.5 text-[23px] leading-[23px] text-main">
        <Skeleton className="w-[120px] h-[23px] rounded mx-auto" />
      </p>
      <div className="mt-[15px] text-xs leading-[12px] text-[#aaaaaa]">
        이번달 소멸 예정 포인트{' '}
        <Skeleton className="w-[30px] h-[12px] rounded inline-block align-middle" />
      </div>
    </div>
  )
}
