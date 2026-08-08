import FetchErrorState from '@/components/ui/FetchErrorState'
import type { OrderMethod } from '@/domains/order'
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

  // DTO → domain model 변환. 클라이언트 컴포넌트에 DTO를 그대로 넘기지 않는다.
  const orderMethods: OrderMethod[] = data.orderMethods.map((item) => ({
    code: item.code,
    name: item.name,
    orderable: item.orderable,
    unavailableReason: item.unavailableReason,
    unavailableReasonName: item.unavailableReasonName,
  }))

  return <OrderMethodContentClient shopId={shopId} orderMethods={orderMethods} />
}
