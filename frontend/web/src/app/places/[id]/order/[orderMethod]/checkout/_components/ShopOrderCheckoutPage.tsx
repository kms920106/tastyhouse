import type { OrderMethodType } from '@/domains/order'
import ShopOrderCheckoutContent from './ShopOrderCheckoutContent'
import ShopOrderCheckoutHeader from './ShopOrderCheckoutHeader'

interface Props {
  shopId: number
  orderMethod: OrderMethodType
}

export default function ShopOrderCheckoutPage({ shopId, orderMethod }: Props) {
  return (
    <>
      <ShopOrderCheckoutHeader />
      <ShopOrderCheckoutContent shopId={shopId} orderMethod={orderMethod} />
    </>
  )
}
