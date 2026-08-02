import FetchErrorState from '@/components/ui/FetchErrorState'
import { COMMON_ERROR_MESSAGES } from '@/constants/errors'
import { useShopInfoDetail } from '@/domains/shop/shop.hook'
import { ShopDetailInfoSkeleton } from './ShopDetailInfoSkeleton'
import ShopDetailInfo from './ShopDetailInfo'

interface Props {
  shopId: number
}

export default function ShopDetailInfoFetcher({ shopId }: Props) {
  const { data, isLoading, error } = useShopInfoDetail(shopId)

  if (isLoading) {
    return <ShopDetailInfoSkeleton />
  }

  if (error) {
    return <FetchErrorState message={COMMON_ERROR_MESSAGES.API_FETCH_ERROR} />
  }

  if (!data?.infoRes.data) {
    return <FetchErrorState message={COMMON_ERROR_MESSAGES.FETCH_ERROR('정보')} />
  }

  const placeInfo = {
    ...data.infoRes.data,
    phoneNumber: data.detailRes.data?.phoneNumber ?? null,
  }

  return <ShopDetailInfo shopInfo={placeInfo} />
}
