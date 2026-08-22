import FetchErrorState from '@/components/ui/FetchErrorState'
import { shopRepository } from '@/domains/shop/shop.repository'
import { COMMON_ERROR_MESSAGES } from '@/constants/errors'
import { ReactNode } from 'react'
import ShopDetailSummaryInfo from './ShopDetailSummaryInfo'

interface Props {
  shopId: number
  bookmarkButton: ReactNode
}

export default async function ShopDetailSummaryServer({ shopId, bookmarkButton }: Props) {
  /*
    가격 뱃지는 상세와 함께 읽는다. 실패해도 화면을 막지 않는다 — 뱃지는 부가 정보이고,
    없으면 컴포넌트가 아무것도 렌더하지 않으므로 기본 정보는 그대로 보여야 한다.
  */
  const [detailResult, priceBadgesResult] = await Promise.all([
    shopRepository.getShopDetail(shopId),
    shopRepository.getShopPriceBadges(shopId),
  ])

  const { error, status, data } = detailResult

  if ((error && status === 404) || !data) {
    return <FetchErrorState message={COMMON_ERROR_MESSAGES.FETCH_ERROR('기본 정보')} />
  }

  if (error) {
    return <FetchErrorState message={COMMON_ERROR_MESSAGES.API_FETCH_ERROR} />
  }

  const {
    id,
    name,
    roadAddress,
    lotAddress,
    rating,
    minOrderAmount,
    minDeliveryTip,
    maxDeliveryTip,
    operatingStatus,
    unavailableReasonName,
  } = data

  return (
    <ShopDetailSummaryInfo
      id={id}
      name={name}
      roadAddress={roadAddress}
      lotAddress={lotAddress}
      rating={rating}
      minOrderAmount={minOrderAmount}
      minDeliveryTip={minDeliveryTip}
      maxDeliveryTip={maxDeliveryTip}
      operatingStatus={operatingStatus}
      unavailableReasonName={unavailableReasonName}
      priceBadges={priceBadgesResult.data ?? null}
      bookmarkButton={bookmarkButton}
    />
  )
}
