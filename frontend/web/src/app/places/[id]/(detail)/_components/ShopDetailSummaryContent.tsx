import { Suspense } from 'react'
import { ShopDetailBookmarkButtonSkeleton } from './ShopDetailBookmarkButtonSkeleton'
import ShopDetailBookmarkButtonServer from './ShopDetailBookmarkButtonServer'
import { ShopDetailSummarySkeleton } from './ShopDetailSummarySkeleton'
import ShopDetailSummaryServer from './ShopDetailSummaryServer'

interface Props {
  shopId: number
}

export default function ShopDetailSummaryContent({ shopId }: Props) {
  return (
    <div className="px-[15px] py-5">
      <Suspense fallback={<ShopDetailSummarySkeleton />}>
        <ShopDetailSummaryServer
          shopId={shopId}
          bookmarkButton={
            <Suspense fallback={<ShopDetailBookmarkButtonSkeleton />}>
              <ShopDetailBookmarkButtonServer shopId={shopId} />
            </Suspense>
          }
        />
      </Suspense>
    </div>
  )
}
