'use client'

import { ReviewPanelWrapper } from '@/components/reviews/ReviewPanel'
import { useShopReviews } from '@/domains/shop/shop.hook'
import { PAGE_PATHS } from '@/lib/paths'

interface Props {
  shopId: number
}

export default function ShopDetailReviewListContent({ shopId }: Props) {
  const { data, isLoading, isError } = useShopReviews(shopId)

  return (
    <ReviewPanelWrapper
      data={data?.data}
      isLoading={isLoading}
      isError={isError}
      viewMoreHref={PAGE_PATHS.PLACE_REVIEWS(shopId)}
    />
  )
}
