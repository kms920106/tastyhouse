import ReviewDetailHeader from '@/components/reviews/ReviewDetailHeader'
import FetchErrorState from '@/components/ui/FetchErrorState'
import { COMMON_ERROR_MESSAGES } from '@/constants/errors'
import { reviewRepository } from '@/domains/review/review.repository'
import ReviewDetailProductContent from './ReviewDetailProductContent'

interface Props {
  reviewId: number
}

export default async function ReviewDetailProductPage({ reviewId }: Props) {
  const { error, status, data } = await reviewRepository.getReviewProductDetail(reviewId)

  if ((error && status === 404) || !data) {
    return <FetchErrorState message={COMMON_ERROR_MESSAGES.FETCH_ERROR('리뷰')} />
  }

  if (error) {
    return <FetchErrorState message={COMMON_ERROR_MESSAGES.API_FETCH_ERROR} />
  }

  const {
    productId,
    productName,
    productImageUrl,
    productPrice,
    content,
    totalRating,
    tasteRating,
    amountRating,
    priceRating,
    atmosphereRating,
    kindnessRating,
    hygieneRating,
    willRevisit,
    memberId,
    memberNickname,
    memberProfileImageUrl,
    createdAt,
    imageUrls,
    tagNames,
  } = data

  return (
    <>
      <ReviewDetailHeader memberNickname={memberNickname} />
      <ReviewDetailProductContent
        productId={productId}
        productName={productName}
        productImageUrl={productImageUrl}
        productPrice={productPrice}
        content={content}
        totalRating={totalRating}
        tasteRating={tasteRating}
        amountRating={amountRating}
        priceRating={priceRating}
        atmosphereRating={atmosphereRating}
        kindnessRating={kindnessRating}
        hygieneRating={hygieneRating}
        willRevisit={willRevisit}
        memberId={memberId}
        memberNickname={memberNickname}
        memberProfileImageUrl={memberProfileImageUrl}
        createdAt={createdAt}
        imageUrls={imageUrls}
        tagNames={tagNames}
      />
    </>
  )
}
