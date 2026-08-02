import FetchErrorState from '@/components/ui/FetchErrorState'
import { reviewRepository } from '@/domains/review/review.repository'
import { COMMON_ERROR_MESSAGES } from '@/constants/errors'
import ReviewCommentListItem from './ReviewCommentListItem'

interface Props {
  reviewId: number
  isLoggedIn: boolean
}

export default async function ReviewCommentList({ reviewId, isLoggedIn }: Props) {
  const { error, status, data } = await reviewRepository.getReviewComments(reviewId)

  if ((error && status === 404) || !data) {
    return <FetchErrorState message={COMMON_ERROR_MESSAGES.FETCH_ERROR('댓글')} />
  }

  if (error) {
    return <FetchErrorState message={COMMON_ERROR_MESSAGES.API_FETCH_ERROR} />
  }

  if (data.comments.length === 0) {
    return (
      <div className="flex flex-col gap-1">
        <p className="text-sm leading-[14px] text-[#999999] text-center">
          아직 작성된 댓글이 없어요.
        </p>
        <p className="text-sm leading-[14px] text-[#999999] text-center">첫 댓글을 남겨보세요!</p>
      </div>
    )
  }

  return data.comments.map((comment) => (
    <ReviewCommentListItem key={comment.id} comment={comment} isLoggedIn={isLoggedIn} />
  ))
}
