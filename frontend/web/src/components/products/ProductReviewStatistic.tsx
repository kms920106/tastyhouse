'use client'

import ReviewRatingDetailItem from '@/components/reviews/ReviewRatingDetailItem'
import ReviewRatingScore from '@/components/reviews/ReviewRatingScore'
import FetchErrorState from '@/components/ui/FetchErrorState'
import { COMMON_ERROR_MESSAGES } from '@/constants/errors'
import { useProductReviewStatistics } from '@/domains/product/product.hook'
import { ProductReviewStatisticSkeleton } from './ProductReviewStatisticSkeleton'

interface Props {
  productId: number
}

export default function ProductReviewStatistic({ productId }: Props) {
  const { data, isLoading, error } = useProductReviewStatistics(productId)

  if (isLoading) {
    return <ProductReviewStatisticSkeleton />
  }

  if (error) {
    return <FetchErrorState message={COMMON_ERROR_MESSAGES.API_FETCH_ERROR} />
  }

  if (!data?.data) {
    return <FetchErrorState message={COMMON_ERROR_MESSAGES.FETCH_ERROR('리뷰 통계')} />
  }

  const {
    totalRating,
    totalReviewCount,
    averageTasteRating,
    averageAmountRating,
    averagePriceRating,
  } = data.data

  const calculatedTotalRating =
    totalRating !== null
      ? totalRating
      : (averageTasteRating + averageAmountRating + averagePriceRating) / 3

  return (
    <div className="flex items-center justify-center gap-[50px] pt-[30px] pb-[21px]">
      <ReviewRatingScore rating={calculatedTotalRating} reviewCount={totalReviewCount} />
      <div className="flex flex-col gap-2.5">
        <ReviewRatingDetailItem label="맛" rating={averageTasteRating} />
        <ReviewRatingDetailItem label="양" rating={averageAmountRating} />
        <ReviewRatingDetailItem label="가격" rating={averagePriceRating} />
      </div>
    </div>
  )
}
