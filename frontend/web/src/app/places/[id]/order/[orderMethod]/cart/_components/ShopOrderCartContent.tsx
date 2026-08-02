import FetchErrorState from '@/components/ui/FetchErrorState'
import { COMMON_ERROR_MESSAGES } from '@/constants/errors'
import { shopRepository } from '@/domains/shop/shop.repository'
import ShopOrderCartContentClient from './ShopOrderCartContentClient'

interface Props {
  shopId: number
}

export default async function ShopOrderCartContent({ shopId }: Props) {
  const { error, data } = await shopRepository.getShopDetail(shopId)

  if (error || !data) {
    return <FetchErrorState message={COMMON_ERROR_MESSAGES.API_FETCH_ERROR} />
  }

  return <ShopOrderCartContentClient shopName={data.name} />
}
