import FetchErrorState from '@/components/ui/FetchErrorState'
import { COMMON_ERROR_MESSAGES } from '@/constants/errors'
import { productRepository } from '@/domains/product/product.repository'
import type { ProductOrderMenuDetailTab } from './ShopOrderMenuDetailProductOptionTabs'
import ShopOrderMenuDetailProductOptionTabs from './ShopOrderMenuDetailProductOptionTabs'

interface Props {
  productId: number
  shopId: number
  tab: ProductOrderMenuDetailTab
}

export default async function ShopOrderMenuDetailOptionSelectorServer({
  productId,
  shopId,
  tab,
}: Props) {
  const { error, data } = await productRepository.getProductReviewCount(productId)

  if (error || !data) {
    return <FetchErrorState message={COMMON_ERROR_MESSAGES.API_FETCH_ERROR} />
  }

  const { reviewCount } = data

  return (
    <ShopOrderMenuDetailProductOptionTabs
      productId={productId}
      shopId={shopId}
      reviewCount={reviewCount}
      tab={tab}
    />
  )
}
