import FetchErrorState from '@/components/ui/FetchErrorState'
import { shopRepository } from '@/domains/shop/shop.repository'
import { COMMON_ERROR_MESSAGES } from '@/constants/errors'
import HomeChoiceSwiper from './HomeChoiceSwiper'

export default async function HomeChoiceShopContent() {
  const { error, status, data } = await shopRepository.getChoiceShops({
    page: 0,
    size: 4,
  })

  if ((error && status === 404) || !data) {
    return <FetchErrorState message={COMMON_ERROR_MESSAGES.FETCH_ERROR('플레이스')} />
  }

  if (error) {
    return <FetchErrorState message={COMMON_ERROR_MESSAGES.API_FETCH_ERROR} />
  }

  return <HomeChoiceSwiper places={data} />
}
