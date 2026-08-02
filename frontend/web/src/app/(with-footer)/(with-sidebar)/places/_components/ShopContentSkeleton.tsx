import { ShopCardSkeleton } from '@/components/shops/ShopCardSkeleton'
import ShopFilterBar from './ShopFilterBar'

export function ShopContentSkeleton() {
  return (
    <div className="min-h-screen px-[15px] py-[30px] pb-[90px]">
      <ShopFilterBar totalCount={0} isLoading />
      <ul className="mt-5 grid grid-cols-2 gap-x-[15px] gap-y-10">
        {[...Array(4)].map((_, i) => (
          <li key={i}>
            <ShopCardSkeleton />
          </li>
        ))}
      </ul>
    </div>
  )
}
