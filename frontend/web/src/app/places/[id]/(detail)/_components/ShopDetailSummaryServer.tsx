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
  const { error, status, data } = await shopRepository.getShopDetail(shopId)

  if ((error && status === 404) || !data) {
    return <FetchErrorState message={COMMON_ERROR_MESSAGES.FETCH_ERROR('기본 정보')} />
  }

  if (error) {
    return <FetchErrorState message={COMMON_ERROR_MESSAGES.API_FETCH_ERROR} />
  }

  const { id, name, roadAddress, lotAddress, rating } = data

  return (
    <ShopDetailSummaryInfo
      id={id}
      name={name}
      roadAddress={roadAddress}
      lotAddress={lotAddress}
      rating={rating}
      bookmarkButton={bookmarkButton}
    />
  )
}
