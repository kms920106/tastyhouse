import ShopImageGallery from '@/components/shops/ShopImageGallery'
import FetchErrorState from '@/components/ui/FetchErrorState'
import { COMMON_ERROR_MESSAGES } from '@/constants/errors'
import { shopRepository } from '@/domains/shop/shop.repository'

interface Props {
  shopId: number
}

export default async function ShopDetailImageGalleryServer({ shopId }: Props) {
  const { error, status, data } = await shopRepository.getShopBanners(shopId)

  if ((error && status === 404) || !data) {
    return <FetchErrorState message={COMMON_ERROR_MESSAGES.FETCH_ERROR('배너')} />
  }

  if (error) {
    return <FetchErrorState message={COMMON_ERROR_MESSAGES.API_FETCH_ERROR} />
  }

  const imageUrls = data.map((banner) => banner.imageUrl)

  return <ShopImageGallery imageUrls={imageUrls} />
}
