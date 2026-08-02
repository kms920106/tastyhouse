import ReviewDetailHeader from '@/components/reviews/ReviewDetailHeader'
import FetchErrorState from '@/components/ui/FetchErrorState'
import { COMMON_ERROR_MESSAGES } from '@/constants/errors'
import { reviewRepository } from '@/domains/review/review.repository'
import { getIsLoggedIn } from '@/lib/auth-config'
import ReviewCommentInputSection from './ReviewCommentInputSection'
import ReviewCommentListSection from './ReviewCommentListSection'
import ReviewInfoSection from './ReviewInfoSection'
import { ReviewReplyProvider } from './ReviewReplyProvider'

interface Props {
  reviewId: number
}

export default async function ReviewDetailPage({ reviewId }: Props) {
  const [{ error, status, data }, isLoggedIn] = await Promise.all([
    reviewRepository.getReviewDetail(reviewId),
    getIsLoggedIn(),
  ])

  if ((error && status === 404) || !data) {
    return <FetchErrorState message={COMMON_ERROR_MESSAGES.FETCH_ERROR('리뷰')} />
  }

  if (error) {
    return <FetchErrorState message={COMMON_ERROR_MESSAGES.API_FETCH_ERROR} />
  }

  const {
    memberId,
    memberNickname,
    memberProfileImageUrl,
    createdAt,
    imageUrls,
    content,
    tagNames,
  } = data

  return (
    <ReviewReplyProvider>
      <ReviewDetailHeader memberNickname={memberNickname} />
      <ReviewInfoSection
        reviewId={reviewId}
        memberId={memberId}
        memberNickname={memberNickname}
        memberProfileImageUrl={memberProfileImageUrl}
        createdAt={createdAt}
        imageUrls={imageUrls}
        content={content}
        tagNames={tagNames}
        isLoggedIn={isLoggedIn}
      />
      <ReviewCommentListSection reviewId={reviewId} isLoggedIn={isLoggedIn} />
      <ReviewCommentInputSection isLoggedIn={isLoggedIn} reviewId={reviewId} />
    </ReviewReplyProvider>
  )
}
