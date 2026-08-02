import FetchErrorState from '@/components/ui/FetchErrorState'
import { COMMON_ERROR_MESSAGES } from '@/constants/errors'
import { productRepository } from '@/domains/product/product.repository'
import ProductDetailReviewTabs from './ProductDetailReviewTabs'

interface Props {
  productId: number
}

export default async function ProductDetailReviewCountServer({ productId }: Props) {
  const { error, data } = await productRepository.getProductReviewCount(productId)

  if (error || !data) {
    return <FetchErrorState message={COMMON_ERROR_MESSAGES.API_FETCH_ERROR} />
  }

  const { reviewCount } = data

  return <ProductDetailReviewTabs productId={productId} reviewCount={reviewCount} />
}
