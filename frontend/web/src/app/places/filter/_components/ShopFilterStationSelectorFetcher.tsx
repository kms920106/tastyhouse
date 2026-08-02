import FetchErrorState from '@/components/ui/FetchErrorState'
import { shopRepository } from '@/domains/shop/shop.repository'
import { COMMON_ERROR_MESSAGES } from '@/constants/errors'
import ShopFilterStationSelector from './ShopFilterStationSelector'

export default async function ShopFilterStationSelectorFetcher() {
  const { error, status, data } = await shopRepository.getShopStations()

  if ((error && status === 404) || !data) {
    return <FetchErrorState message={COMMON_ERROR_MESSAGES.FETCH_ERROR('지하철역')} />
  }

  if (error) {
    return <FetchErrorState message={COMMON_ERROR_MESSAGES.API_FETCH_ERROR} />
  }

  return <ShopFilterStationSelector stations={data} />
}
