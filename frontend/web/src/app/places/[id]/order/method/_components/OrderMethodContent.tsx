import FetchErrorState from '@/components/ui/FetchErrorState'
import { shopRepository } from '@/domains/shop/shop.repository'
import { COMMON_ERROR_MESSAGES } from '@/constants/errors'
import OrderMethodContentClient from './OrderMethodContentClient'

interface Props {
  shopId: number
}

export default async function OrderMethodContent({ shopId }: Props) {
  const { error, status, data } = await shopRepository.getShopOrderMethods(shopId)

  if ((error && status === 404) || !data) {
    return <FetchErrorState message={COMMON_ERROR_MESSAGES.FETCH_ERROR('주문 수단')} />
  }

  if (error) {
    return <FetchErrorState message={COMMON_ERROR_MESSAGES.API_FETCH_ERROR} />
  }

  const { orderMethods } = data

  return <OrderMethodContentClient shopId={shopId} orderMethods={orderMethods} />
}
