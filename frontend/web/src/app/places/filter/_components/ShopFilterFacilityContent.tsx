import { FacilitySelectorSkeleton } from '@/components/shops/FacilitySelectorSkeleton'
import { Suspense } from 'react'
import ShopFilterFacilitySelectorFetcher from './ShopFilterFacilitySelectorFetcher'

export default function ShopFilterFacilityContent() {
  return (
    <div className="px-[15px] py-[30px]">
      <h2 className="mb-[15px] text-sm leading-[14px]">편의시설</h2>
      <Suspense fallback={<FacilitySelectorSkeleton />}>
        <ShopFilterFacilitySelectorFetcher />
      </Suspense>
    </div>
  )
}
