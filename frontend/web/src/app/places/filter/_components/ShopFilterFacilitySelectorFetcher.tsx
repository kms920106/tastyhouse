import FetchErrorState from '@/components/ui/FetchErrorState'
import { shopService } from '@/domains/shop/shop.service'
import { COMMON_ERROR_MESSAGES } from '@/constants/errors'
import ShopFilterFacilitySelector from './ShopFilterFacilitySelector'

export default async function ShopFilterFacilitySelectorFetcher() {
  const { error, status, data } = await shopService.getShopAmenities()

  if ((error && status === 404) || !data) {
    return <FetchErrorState message={COMMON_ERROR_MESSAGES.FETCH_ERROR('편의시설')} />
  }

  if (error) {
    return <FetchErrorState message={COMMON_ERROR_MESSAGES.API_FETCH_ERROR} />
  }

  return <ShopFilterFacilitySelector amenities={data} />
}
