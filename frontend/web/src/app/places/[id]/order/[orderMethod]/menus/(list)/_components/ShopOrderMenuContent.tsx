import SectionStack from '@/components/ui/SectionStack'
import type { OrderMethodType } from '@/domains/order'
import ShopOrderMenuList from './ShopOrderMenuList'

interface Props {
  shopId: number
  orderMethod: OrderMethodType
}

export default function ShopOrderMenuContent({ shopId, orderMethod }: Props) {
  return (
    <SectionStack>
      <ShopOrderMenuList shopId={shopId} orderMethod={orderMethod} />
    </SectionStack>
  )
}
