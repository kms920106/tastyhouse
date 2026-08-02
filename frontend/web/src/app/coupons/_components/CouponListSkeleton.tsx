import { Skeleton } from '@/components/ui/shadcn/skeleton'
import Image from 'next/image'

function CouponListItemSkeleton() {
  return (
    <div className="relative w-full">
      <Image
        src="/images/coupon/coupon.png"
        alt="쿠폰"
        width={690}
        height={285}
        className="w-full h-auto"
      />
      <div className="absolute inset-0 flex">
        <div className="flex flex-col justify-center w-[70%] px-[20px] py-7 overflow-hidden">
          <Skeleton className="h-[21px] w-[80px]" />
          <Skeleton className="mt-[15px] h-[18px] w-full" />
          <div className="flex flex-col gap-1 mt-2.5">
            <Skeleton className="h-[14px] w-[150px]" />
            <Skeleton className="h-[14px] w-[120px]" />
            <Skeleton className="h-[14px] w-[200px]" />
          </div>
        </div>
        <div className="flex items-center justify-center w-[30%]">
          <Skeleton className="h-[16px] w-[50px]" />
        </div>
      </div>
    </div>
  )
}

export function CouponListSkeleton() {
  return Array.from({ length: 5 }).map((_, index) => (
    <CouponListItemSkeleton key={index} />
  ))
}
