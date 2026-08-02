import AppPrimaryButton from '@/components/ui/AppPrimaryButton'
import type { OrderMethodType } from '@/domains/order'
import { PAGE_PATHS } from '@/lib/paths'
import Link from 'next/link'

interface Props {
  shopId: number
  orderMethod: OrderMethodType
}

export default function ShopOrderCartLinkButton({ shopId, orderMethod }: Props) {
  return (
    <Link href={PAGE_PATHS.ORDER_CHECKOUT(shopId, orderMethod)}>
      <AppPrimaryButton>주문하기</AppPrimaryButton>
    </Link>
  )
}
