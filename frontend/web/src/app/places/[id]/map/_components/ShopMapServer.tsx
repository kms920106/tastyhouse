import FetchErrorState from '@/components/ui/FetchErrorState'
import { COMMON_ERROR_MESSAGES } from '@/constants/errors'
import { shopRepository } from '@/domains/shop/shop.repository'
import ShopMap from './ShopMap'
import ShopMapHeader from './ShopMapHeader'

interface Props {
  shopId: number
}

export default async function ShopMapServer({ shopId }: Props) {
  const { error, status, data } = await shopRepository.getShopDetail(shopId)

  if ((error && status === 404) || !data) {
    return (
      <div className="flex flex-col h-screen">
        <ShopMapHeader name="" />
        <FetchErrorState message={COMMON_ERROR_MESSAGES.FETCH_ERROR('위치')} />
      </div>
    )
  }

  if (error) {
    return (
      <div className="flex flex-col h-screen">
        <ShopMapHeader name="" />
        <FetchErrorState message={COMMON_ERROR_MESSAGES.API_FETCH_ERROR} />
      </div>
    )
  }

  const { name, latitude, longitude } = data

  return (
    <div className="flex flex-col h-screen">
      <ShopMapHeader name={name} />
      <ShopMap name={name} latitude={latitude} longitude={longitude} />
    </div>
  )
}
