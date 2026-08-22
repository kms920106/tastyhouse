import ShopImageGallery from '@/components/shops/ShopImageGallery'
import FetchErrorState from '@/components/ui/FetchErrorState'
import { COMMON_ERROR_MESSAGES } from '@/constants/errors'
import { shopRepository } from '@/domains/shop/shop.repository'

interface Props {
  shopId: number
}

/**
 * 가게 상세 최상단 이미지 갤러리.
 *
 * 메뉴모음컷이 있으면 그것을 우선 노출한다 — PDF 기준 "가게를 클릭한 고객에게 가장 먼저,
 * 가장 상단에서 보여지는 이미지"가 메뉴모음컷이기 때문이다. 승인·정렬은 서버가 끝낸 상태로
 * 내려오므로 프론트는 재정렬하지 않는다.
 *
 * 메뉴모음컷 조회가 실패하거나 결과가 비면 기존 배너 갤러리 동작을 그대로 유지한다 — 신규 기능의
 * 장애가 이미 잘 동작하던 최상단 배너를 깨뜨려서는 안 된다.
 */
export default async function ShopDetailImageGalleryServer({ shopId }: Props) {
  const [menuCollectionResult, bannerResult] = await Promise.all([
    shopRepository.getShopMenuCollectionImages(shopId),
    shopRepository.getShopBanners(shopId),
  ])

  // imageUrl 이 null 인 레코드(원본 파일이 정리된 경우)는 갤러리에서 걸러낸다.
  const menuCollectionImageUrls = (menuCollectionResult.data ?? [])
    .map((image) => image.imageUrl)
    .filter((imageUrl): imageUrl is string => !!imageUrl)

  if (menuCollectionImageUrls.length > 0) {
    return <ShopImageGallery imageUrls={menuCollectionImageUrls} />
  }

  const { error, status, data } = bannerResult

  if ((error && status === 404) || !data) {
    return <FetchErrorState message={COMMON_ERROR_MESSAGES.FETCH_ERROR('배너')} />
  }

  if (error) {
    return <FetchErrorState message={COMMON_ERROR_MESSAGES.API_FETCH_ERROR} />
  }

  const imageUrls = data.map((banner) => banner.imageUrl)

  return <ShopImageGallery imageUrls={imageUrls} />
}
