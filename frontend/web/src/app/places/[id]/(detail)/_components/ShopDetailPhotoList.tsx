import ReviewImageGallery from '@/components/reviews/ReviewImageGallery'
import FetchErrorState from '@/components/ui/FetchErrorState'
import { COMMON_ERROR_MESSAGES } from '@/constants/errors'
import { useShopPhotos } from '@/domains/shop/shop.hook'
import { ShopDetailPhotoListSkeleton } from './ShopDetailPhotoListSkeleton'

interface Props {
  shopId: number
}

export default function ShopDetailPhotoList({ shopId }: Props) {
  const { data, isLoading, error } = useShopPhotos(shopId)

  if (isLoading) {
    return <ShopDetailPhotoListSkeleton />
  }

  if (error) {
    return <FetchErrorState message={COMMON_ERROR_MESSAGES.API_FETCH_ERROR} />
  }

  if (!data?.data) {
    return <FetchErrorState message={COMMON_ERROR_MESSAGES.FETCH_ERROR('사진')} />
  }

  const categories = data.data

  return categories.map((category) => (
    <div key={category.name} className="pt-[30px]">
      <h3 className="mb-[15px] text-sm leading-[14px]">{category.name}</h3>
      <ReviewImageGallery imageUrls={category.imageUrls} />
    </div>
  ))
}
