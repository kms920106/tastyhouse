import { Suspense } from 'react'
import ShopMapServer from './ShopMapServer'
import { ShopMapSkeleton } from './ShopMapSkeleton'

interface Props {
  shopId: number
}

export default function ShopMapPage({ shopId }: Props) {
  return (
    <Suspense fallback={<ShopMapSkeleton />}>
      <ShopMapServer shopId={shopId} />
    </Suspense>
  )
}
