import { Suspense } from 'react'
import CouponList from './CouponList'
import { CouponListSkeleton } from './CouponListSkeleton'

export default function CouponContent() {
  return (
    <div className="px-[15px] py-[30px]">
      <div className="flex flex-col gap-5">
        <Suspense fallback={<CouponListSkeleton />}>
          <CouponList />
        </Suspense>
      </div>
    </div>
  )
}
