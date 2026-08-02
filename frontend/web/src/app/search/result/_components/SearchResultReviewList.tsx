'use client'

import ReviewThumbnail from '@/components/reviews/ReviewThumbnail'
import FetchErrorState from '@/components/ui/FetchErrorState'
import { COMMON_ERROR_MESSAGES } from '@/constants/errors'
import { useSearchReviewsInfinite } from '@/domains/search/search.hook'
import { useIntersectionObserver } from '@/hooks/useIntersectionObserver'
import { PAGE_PATHS } from '@/lib/paths'
import Link from 'next/link'
import { useEffect } from 'react'
import SearchResultEmptyState from './SearchResultEmptyState'
import SearchResultReviewListSkeleton from './SearchResultReviewGridSkeleton'

interface Props {
  query: string
}

export default function SearchResultReviewList({ query }: Props) {
  const { data, fetchNextPage, hasNextPage, isFetchingNextPage, isLoading, isError } =
    useSearchReviewsInfinite(query)

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

  if (isLoading)
    return (
      <div className="px-[15px] py-6">
        <SearchResultReviewListSkeleton />
      </div>
    )
  if (isError) return <FetchErrorState message={COMMON_ERROR_MESSAGES.API_FETCH_ERROR} />

  const totalElements = data?.pages[0]?.pagination?.totalElements ?? 0
  if (totalElements === 0) return <SearchResultEmptyState query={query} label="리뷰" />

  const reviews = data?.pages.flatMap((page) => page.data ?? []) ?? []

  return (
    <div className="px-[15px] py-6">
      <div className="grid grid-cols-3 gap-2">
        {reviews.map((review, index) => (
          <Link key={review.id} href={PAGE_PATHS.REVIEW_DETAIL(review.id)}>
            <ReviewThumbnail imageUrl={review.imageUrl} priority={index === 0} />
          </Link>
        ))}
      </div>
      {isFetchingNextPage && <SearchResultReviewListSkeleton count={3} />}
      <div ref={targetRef} className="h-1" aria-hidden="true" />
    </div>
  )
}
