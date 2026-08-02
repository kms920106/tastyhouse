import { ShopCardSkeleton } from '@/components/shops/ShopCardSkeleton'

export default function BestShopListSkeleton({ count = 4 }: { count?: number }) {
  return (
    <div className="grid grid-cols-2 gap-x-[15px] gap-y-10">
      {Array.from({ length: count }).map((_, i) => (
        <ShopCardSkeleton key={i} />
      ))}
    </div>
  )
}
