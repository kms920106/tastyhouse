'use client'

import ReviewThumbnail from '@/components/reviews/ReviewThumbnail'
import FetchErrorState from '@/components/ui/FetchErrorState'
import { COMMON_ERROR_MESSAGES } from '@/constants/errors'
import { useSearchReviewsPreview } from '@/domains/search/search.hook'
import { PAGE_PATHS } from '@/lib/paths'
import Link from 'next/link'
import SearchResultEmptyState from './SearchResultEmptyState'
import SearchResultReviewListSkeleton from './SearchResultReviewGridSkeleton'

interface Props {
  query: string
}

export default function SearchResultAllReviewList({ query }: Props) {
  const { data, isLoading, isError } = useSearchReviewsPreview(query)

  if (isLoading) return <SearchResultReviewListSkeleton count={9} />
  if (isError) return <FetchErrorState message={COMMON_ERROR_MESSAGES.FETCH_ERROR('리뷰')} />
  if (!data?.data || data.data.length === 0)
    return <SearchResultEmptyState query={query} label="리뷰" />

  return (
    <div className="grid grid-cols-3 gap-2">
      {data.data.map((review, index) => (
        <Link key={review.id} href={PAGE_PATHS.REVIEW_DETAIL(review.id)}>
          <ReviewThumbnail imageUrl={review.imageUrl} priority={index === 0} />
        </Link>
      ))}
    </div>
  )
}
