import FetchErrorState from '@/components/ui/FetchErrorState'
import { reviewRepository } from '@/domains/review/review.repository'
import { COMMON_ERROR_MESSAGES } from '@/constants/errors'
import HomeBestReviewSwiper from './HomeBestReviewSwiper'

export default async function HomeBestReviewContent() {
  const { error, status, data } = await reviewRepository.getBestReviews({
    page: 0,
    size: 5,
  })

  if ((error && status === 404) || !data) {
    return <FetchErrorState message={COMMON_ERROR_MESSAGES.FETCH_ERROR('리뷰')} />
  }

  if (error) {
    return <FetchErrorState message={COMMON_ERROR_MESSAGES.API_FETCH_ERROR} />
  }

  return <HomeBestReviewSwiper reviews={data} />
}
