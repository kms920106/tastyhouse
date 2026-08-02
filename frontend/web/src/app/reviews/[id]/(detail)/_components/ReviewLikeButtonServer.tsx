import ReviewLikeButtonError from '@/app/reviews/[id]/(detail)/_components/ReviewLikeButtonError'
import { reviewRepository } from '@/domains/review/review.repository'
import { getIsLoggedIn } from '@/lib/auth-config'
import { PAGE_PATHS } from '@/lib/paths'
import Link from 'next/link'
import ReviewLikeButton from './ReviewLikeButton'
import ReviewLikeButtonClient from './ReviewLikeButtonClient'

interface Props {
  reviewId: number
}

export default async function ReviewLikeButtonServer({ reviewId }: Props) {
  const isLoggedIn = await getIsLoggedIn()

  if (!isLoggedIn) {
    return (
      <Link href={PAGE_PATHS.AUTH_LOGIN}>
        <ReviewLikeButton isLiked={false} />
      </Link>
    )
  }

  const { error, data } = await reviewRepository.getReviewLike(reviewId)

  if (error) {
    return <ReviewLikeButtonError />
  }

  if (!data) {
    return <ReviewLikeButtonError />
  }

  return <ReviewLikeButtonClient initialIsLiked={data.liked} reviewId={reviewId} />
}
