'use client'

import ReviewList from '@/components/reviews/ReviewList'
import ReviewListSkeleton from '@/components/reviews/ReviewListSkeleton'
import FetchErrorState from '@/components/ui/FetchErrorState'
import { COMMON_ERROR_MESSAGES } from '@/constants/errors'
import { useMemberReviews } from '@/domains/member/member.hook'

interface Props {
  memberId: number
}

export default function MemberDetailReviewTabContent({ memberId }: Props) {
  const { data, isLoading, error } = useMemberReviews(memberId)

  if (isLoading) {
    return <ReviewListSkeleton />
  }

  if (error || !data) {
    return <FetchErrorState message={COMMON_ERROR_MESSAGES.FETCH_ERROR('리뷰')} />
  }

  const { reviews, hasMore } = data

  return <ReviewList reviews={reviews} hasMoreReviews={hasMore} />
}
