import type { OrderMethodType } from '@/domains/order'
import ShopOrderCheckoutContent from './ShopOrderCheckoutContent'
import ShopOrderCheckoutHeader from './ShopOrderCheckoutHeader'

interface Props {
  shopId: number
  orderMethod: OrderMethodType
  /** 장바구니에서 넘어온 수령 예약 시각(슬롯 startAt). 미선택이면 null */
  scheduledAt: string | null
}

export default function ShopOrderCheckoutPage({ shopId, orderMethod, scheduledAt }: Props) {
  return (
    <>
      <ShopOrderCheckoutHeader />
      <ShopOrderCheckoutContent
        shopId={shopId}
        orderMethod={orderMethod}
        scheduledAt={scheduledAt}
      />
    </>
  )
}
