import type { OrderMethodType } from '@/domains/order'
import ShopOrderMenuContent from './ShopOrderMenuContent'
import ShopOrderMenuFooter from './ShopOrderMenuFooter'
import ShopOrderMenuHeader from './ShopOrderMenuHeader'

interface Props {
  shopId: number
  orderMethod: OrderMethodType
}

export default function ShopOrderMenuPage({ shopId, orderMethod }: Props) {
  return (
    <>
      <ShopOrderMenuHeader />
      <ShopOrderMenuContent shopId={shopId} orderMethod={orderMethod} />
      <ShopOrderMenuFooter shopId={shopId} orderMethod={orderMethod} />
    </>
  )
}
