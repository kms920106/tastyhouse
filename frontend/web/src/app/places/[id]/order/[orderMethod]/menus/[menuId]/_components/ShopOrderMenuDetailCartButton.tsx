import HeaderIconLink from '@/components/layouts/header-parts/HeaderIconLink'
import type { OrderMethodType } from '@/domains/order'
import { PAGE_PATHS } from '@/lib/paths'
import Icon from '@/components/ui/Icon'

interface Props {
  shopId: number
  count: number
  orderMethod: OrderMethodType
}

export default function ShopOrderMenuDetailCartButton({ shopId, count, orderMethod }: Props) {
  return (
    <HeaderIconLink href={PAGE_PATHS.ORDER_CART(shopId, orderMethod)}>
      <div className="relative w-[22px] h-[22px] flex items-center justify-center">
        <Icon name="order/cart-black" className="z-1" />
        <span className="absolute top-1.5 flex items-center justify-center w-4 h-4 text-[10px]">
          {count > 99 ? '99+' : count}
        </span>
      </div>
    </HeaderIconLink>
  )
}
