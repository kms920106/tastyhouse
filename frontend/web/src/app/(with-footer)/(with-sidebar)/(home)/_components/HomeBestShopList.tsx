import { ShopBestListItem } from '@/components/shops/ShopBestListItem'
import FetchErrorState from '@/components/ui/FetchErrorState'
import ViewMoreButton from '@/components/ui/ViewMoreButton'
import { COMMON_ERROR_MESSAGES } from '@/constants/errors'
import { shopRepository } from '@/domains/shop/shop.repository'
import { PAGE_PATHS } from '@/lib/paths'

export default async function HomeBestShopList() {
  const { error, status, data } = await shopRepository.getBestShops({
    page: 0,
    size: 4,
  })

  if ((error && status === 404) || !data) {
    return <FetchErrorState message={COMMON_ERROR_MESSAGES.FETCH_ERROR('플레이스')} />
  }

  if (error) {
    return <FetchErrorState message={COMMON_ERROR_MESSAGES.API_FETCH_ERROR} />
  }

  if (data.length === 0) {
    return null
  }

  return (
    <>
      <ul className="grid grid-cols-2 gap-x-[15px] gap-y-10 mb-10">
        {data.map((shop) => (
          <ShopBestListItem
            key={shop.id}
            id={shop.id}
            name={shop.name}
            imageUrl={shop.imageUrl}
            stationName={shop.stationName}
            rating={shop.rating}
            foodTypes={shop.foodTypes}
          />
        ))}
      </ul>
      <div className="flex justify-center">
        <ViewMoreButton href={PAGE_PATHS.PLACE_BEST} />
      </div>
    </>
  )
}
