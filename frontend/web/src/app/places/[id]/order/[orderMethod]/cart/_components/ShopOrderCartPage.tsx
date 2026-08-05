import type { OrderMethodType } from '@/domains/order'
import { Suspense } from 'react'
import ShopOrderCartContent from './ShopOrderCartContent'
import ShopOrderCartContentSkeleton from './ShopOrderCartContentSkeleton'
import ShopOrderCartHeader from './ShopOrderCartHeader'

interface Props {
  shopId: number
  orderMethod: OrderMethodType
}

/**
 * 주문하기 CTA(StickyFooter)는 선택 상품 합계에 따라 최소주문금액 미달 여부를 판정해야 하므로
 * 선택 상태를 보유한 ShopOrderCartContentClient 안에서 렌더한다.
 */
export default function ShopOrderCartPage({ shopId, orderMethod }: Props) {
  return (
    <>
      <ShopOrderCartHeader />
      <Suspense fallback={<ShopOrderCartContentSkeleton />}>
        <ShopOrderCartContent shopId={shopId} orderMethod={orderMethod} />
      </Suspense>
    </>
  )
}
