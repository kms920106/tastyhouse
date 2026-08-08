import { memberRepository } from '@/domains/member/member.repository'
import type { OrderMethodType } from '@/domains/order'
import { shopRepository } from '@/domains/shop/shop.repository'
import ShopOrderCheckoutContentClient from './ShopOrderCheckoutContentClient'
import FetchErrorState from '@/components/ui/FetchErrorState'
import { COMMON_ERROR_MESSAGES } from '@/constants/errors'

interface Props {
  shopId: number
  orderMethod: OrderMethodType
  /** 장바구니에서 넘어온 수령 예약 시각(슬롯 startAt). 미선택이면 null */
  scheduledAt: string | null
}

export default async function ShopOrderCheckoutContent({
  shopId,
  orderMethod,
  scheduledAt,
}: Props) {
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
      scheduledOrderEnabled={shopResult.data.scheduledOrderEnabled}
      initialScheduledAt={scheduledAt}
    />
  )
}
