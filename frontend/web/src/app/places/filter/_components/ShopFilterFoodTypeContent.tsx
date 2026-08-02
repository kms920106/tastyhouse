import { Suspense } from 'react'
import ShopFilterFoodTypeSelectorFetcher from './ShopFilterFoodTypeSelectorFetcher'
import { ShopFilterFoodTypeSelectorSkeleton } from './ShopFilterFoodTypeSelectorSkeleton'

export default function ShopFilterFoodTypeContent() {
  return (
    <div className="px-[15px] py-[30px]">
      <h2 className="mb-[15px] text-sm leading-[14px]">음식 종류</h2>
      <Suspense fallback={<ShopFilterFoodTypeSelectorSkeleton />}>
        <ShopFilterFoodTypeSelectorFetcher />
      </Suspense>
    </div>
  )
}
