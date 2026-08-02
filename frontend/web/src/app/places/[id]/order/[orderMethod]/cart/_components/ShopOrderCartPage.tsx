import StickyFooter from '@/components/ui/StickyFooter'
import type { OrderMethodType } from '@/domains/order'
import { Suspense } from 'react'
import ShopOrderCartContent from './ShopOrderCartContent'
import ShopOrderCartContentSkeleton from './ShopOrderCartContentSkeleton'
import ShopOrderCartHeader from './ShopOrderCartHeader'
import ShopOrderCartLinkButton from './ShopOrderCartLinkButton'

interface Props {
  shopId: number
  orderMethod: OrderMethodType
}

export default function ShopOrderCartPage({ shopId, orderMethod }: Props) {
  return (
    <>
      <ShopOrderCartHeader />
      <Suspense fallback={<ShopOrderCartContentSkeleton />}>
        <ShopOrderCartContent shopId={shopId} />
      </Suspense>
      <StickyFooter>
        <div className="px-[15px] py-2.5 bg-[#f9f9f9]">
          <ShopOrderCartLinkButton shopId={shopId} orderMethod={orderMethod} />
        </div>
      </StickyFooter>
    </>
  )
}
