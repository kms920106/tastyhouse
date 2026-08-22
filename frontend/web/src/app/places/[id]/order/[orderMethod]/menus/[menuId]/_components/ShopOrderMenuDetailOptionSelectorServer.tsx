import FetchErrorState from '@/components/ui/FetchErrorState'
import { COMMON_ERROR_MESSAGES } from '@/constants/errors'
import type { OrderMethodType } from '@/domains/order'
import { productRepository } from '@/domains/product/product.repository'
import type { ProductOrderMenuDetailTab } from './ShopOrderMenuDetailProductOptionTabs'
import ShopOrderMenuDetailProductOptionTabs from './ShopOrderMenuDetailProductOptionTabs'

interface Props {
  productId: number
  shopId: number
  tab: ProductOrderMenuDetailTab
  /** 가격 해석 기준. 서버가 이 값으로 `prices[].price` 를 확정해 내려준다 */
  orderMethod: OrderMethodType
}

export default async function ShopOrderMenuDetailOptionSelectorServer({
  productId,
  shopId,
  tab,
  orderMethod,
}: Props) {
  /*
    리뷰 수와 가격 목록을 함께 읽는다. 가격 조회가 실패해도 화면을 막지 않는다 —
    가격이 1개인 메뉴(대부분)는 선택 UI 가 필요 없으므로 옵션 선택은 그대로 되어야 한다.
    가격이 여러 개인 메뉴에서 조회가 실패하면 선택 UI 가 안 뜨고 `priceId` 없이 담기는데,
    서버가 `sort=0` 행으로 해석하므로 주문 자체는 성립한다.
  */
  const [reviewCountResult, detailResult] = await Promise.all([
    productRepository.getProductReviewCount(productId),
    productRepository.getProductById(productId, orderMethod),
  ])

  if (reviewCountResult.error || !reviewCountResult.data) {
    return <FetchErrorState message={COMMON_ERROR_MESSAGES.API_FETCH_ERROR} />
  }

  const { reviewCount } = reviewCountResult.data

  return (
    <ShopOrderMenuDetailProductOptionTabs
      productId={productId}
      shopId={shopId}
      reviewCount={reviewCount}
      tab={tab}
      prices={detailResult.data?.prices}
    />
  )
}
