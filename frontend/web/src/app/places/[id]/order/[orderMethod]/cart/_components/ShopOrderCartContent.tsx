import FetchErrorState from '@/components/ui/FetchErrorState'
import { COMMON_ERROR_MESSAGES } from '@/constants/errors'
import type { OrderMethodType } from '@/domains/order'
import { shopRepository } from '@/domains/shop/shop.repository'
import ShopOrderCartContentClient from './ShopOrderCartContentClient'

interface Props {
  shopId: number
  orderMethod: OrderMethodType
}

export default async function ShopOrderCartContent({ shopId, orderMethod }: Props) {
  const { error, data } = await shopRepository.getShopDetail(shopId)

  if (error || !data) {
    return <FetchErrorState message={COMMON_ERROR_MESSAGES.API_FETCH_ERROR} />
  }

  return (
    <ShopOrderCartContentClient
      shopId={shopId}
      shopName={data.name}
      orderMethod={orderMethod}
      minOrderAmount={data.minOrderAmount}
    />
  )
}
