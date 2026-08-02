import { Suspense } from 'react'
import ShopFilterStationSelectorFetcher from './ShopFilterStationSelectorFetcher'
import { ShopFilterStationSelectorSkeleton } from './ShopFilterStationSelectorSkeleton'

export default function ShopFilterStationContent() {
  return (
    <div className="px-[15px] py-5">
      <h2 className="mb-[15px] text-sm leading-[14px]">지하철역</h2>
      <Suspense fallback={<ShopFilterStationSelectorSkeleton />}>
        <ShopFilterStationSelectorFetcher />
      </Suspense>
    </div>
  )
}
