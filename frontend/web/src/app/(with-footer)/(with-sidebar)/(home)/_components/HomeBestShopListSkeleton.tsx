import { ShopCardSkeleton } from '@/components/shops/ShopCardSkeleton'

export function HomeBestShopListSkeleton() {
  return (
    <div className="grid grid-cols-2 gap-x-[15px] gap-y-10 mb-10">
      {[...Array(4)].map((_, i) => (
        <ShopCardSkeleton key={i} />
      ))}
    </div>
  )
}
