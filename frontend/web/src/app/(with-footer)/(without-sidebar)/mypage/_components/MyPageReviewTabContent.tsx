'use client'

import ReviewList from '@/components/reviews/ReviewList'
import ReviewListSkeleton from '@/components/reviews/ReviewListSkeleton'
import FetchErrorState from '@/components/ui/FetchErrorState'
import { COMMON_ERROR_MESSAGES } from '@/constants/errors'
import { useMyReviews } from '@/domains/member/member.hook'

export default function MyPageReviewTabContent() {
  const { data, isLoading, error } = useMyReviews()

  if (isLoading) {
    return <ReviewListSkeleton />
  }

  if (error || !data) {
    return <FetchErrorState message={COMMON_ERROR_MESSAGES.FETCH_ERROR('리뷰')} />
  }

  const { reviews, hasMore } = data

  return <ReviewList reviews={reviews} hasMoreReviews={hasMore} />
}
