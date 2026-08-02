import FetchErrorState from '@/components/ui/FetchErrorState'
import { shopService } from '@/domains/shop/shop.service'
import { COMMON_ERROR_MESSAGES } from '@/constants/errors'
import ShopFilterFoodTypeSelector from './ShopFilterFoodTypeSelector'

export default async function ShopFilterFoodTypeSelectorFetcher() {
  const { error, status, data } = await shopService.getShopFoodTypes()

  if ((error && status === 404) || !data) {
    return <FetchErrorState message={COMMON_ERROR_MESSAGES.FETCH_ERROR('음식 종류')} />
  }

  if (error) {
    return <FetchErrorState message={COMMON_ERROR_MESSAGES.API_FETCH_ERROR} />
  }

  return <ShopFilterFoodTypeSelector foods={data} />
}
