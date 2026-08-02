'use client'

import FetchErrorState from '@/components/ui/FetchErrorState'
import type { ReviewType } from '@/domains/review'
import { useLatestReviews } from '@/domains/review/review.hook'
import { useIntersectionObserver } from '@/hooks/useIntersectionObserver'
import { COMMON_ERROR_MESSAGES } from '@/constants/errors'
import { useEffect } from 'react'
import LatestReviewListItem from './LatestReviewListItem'
import { LatestReviewListSkeleton } from './LatestReviewListSkeleton'

interface Props {
  reviewType: ReviewType
}

export default function LatestReviewList({ reviewType }: Props) {
  const { data, fetchNextPage, hasNextPage, isFetchingNextPage, isLoading, isError, error } =
    useLatestReviews(reviewType)

  const { targetRef, isIntersecting, resetIntersecting } = useIntersectionObserver({
    threshold: 0.1,
    rootMargin: '100px',
    enabled: hasNextPage && !isFetchingNextPage,
  })

  useEffect(() => {
    if (isIntersecting && hasNextPage && !isFetchingNextPage) {
      resetIntersecting()
      fetchNextPage()
    }
  }, [isIntersecting, hasNextPage, isFetchingNextPage, fetchNextPage, resetIntersecting])

  if (isLoading) {
    return <LatestReviewListSkeleton />
  }

  if (isError) {
    return <FetchErrorState message={error?.message || COMMON_ERROR_MESSAGES.API_FETCH_ERROR} />
  }

  if (!data?.pages || data.pages.length === 0) {
    return <FetchErrorState message={COMMON_ERROR_MESSAGES.FETCH_ERROR('리뷰')} />
  }

  // 모든 페이지의 리뷰를 평탄화
  const reviews = data.pages.flatMap((page) => {
    if (!page.data || !Array.isArray(page.data)) {
      return []
    }
    return page.data
  })

  // 리뷰가 없는 경우
  if (reviews.length === 0) {
    return <div className="py-10 bg-white text-center text-sm text-[#aaaaaa]">리뷰가 없습니다.</div>
  }

  return (
    <>
      {reviews.map((review) => (
        <LatestReviewListItem key={review.id} review={review} />
      ))}
      {isFetchingNextPage && <LatestReviewListSkeleton />}
      <div ref={targetRef} className="h-1" aria-hidden="true" />
    </>
  )
}
