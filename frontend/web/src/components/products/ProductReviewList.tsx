'use client'

import { ReviewPanelWrapper } from '@/components/reviews/ReviewPanel'
import { useProductReviews } from '@/domains/product/product.hook'
import { PAGE_PATHS } from '@/lib/paths'

interface Props {
  productId: number
}

export default function ProductReviewList({ productId }: Props) {
  const { data, isLoading, isError } = useProductReviews(productId)

  return (
    <ReviewPanelWrapper
      data={data?.data}
      isLoading={isLoading}
      isError={isError}
      viewMoreHref={PAGE_PATHS.PRODUCT_REVIEWS(productId)}
    />
  )
}
