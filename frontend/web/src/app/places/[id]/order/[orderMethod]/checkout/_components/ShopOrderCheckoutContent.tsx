import { memberRepository } from '@/domains/member/member.repository'
import type { OrderMethodType } from '@/domains/order'
import { shopRepository } from '@/domains/shop/shop.repository'
import ShopOrderCheckoutContentClient from './ShopOrderCheckoutContentClient'
import FetchErrorState from '@/components/ui/FetchErrorState'
import { COMMON_ERROR_MESSAGES } from '@/constants/errors'

interface Props {
  shopId: number
  orderMethod: OrderMethodType
}

export default async function ShopOrderCheckoutContent({ shopId, orderMethod }: Props) {
  const [shopResult, memberResult, couponsResult, usablePointResult] = await Promise.all([
    shopRepository.getShopDetail(shopId),
    memberRepository.getMyPersonalInfo(),
    memberRepository.getMyAvailableCoupons(),
    memberRepository.getMyUsablePoint(),
  ])

  if (
    shopResult.error ||
    !shopResult.data ||
    memberResult.error ||
    !memberResult.data ||
    couponsResult.error ||
    !couponsResult.data ||
    usablePointResult.error ||
    !usablePointResult.data
  ) {
    return <FetchErrorState message={COMMON_ERROR_MESSAGES.API_FETCH_ERROR} />
  }

  return (
    <ShopOrderCheckoutContentClient
      shop={shopResult.data}
      member={memberResult.data}
      availableCoupons={couponsResult.data}
      usablePoints={usablePointResult.data.usablePoints}
      orderMethod={orderMethod}
    />
  )
}
