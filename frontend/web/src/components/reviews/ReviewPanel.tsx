'use client'

import ReviewFilter from '@/components/reviews/ReviewFilter'
import ReviewListItem from '@/components/reviews/ReviewListItem'
import ReviewPanelSkeleton from '@/components/reviews/ReviewPanelSkeleton'
import FetchErrorState from '@/components/ui/FetchErrorState'
import ViewMoreButton from '@/components/ui/ViewMoreButton'
import type { ReviewPanelData } from '@/hooks/useReviewPanel'
import { useReviewPanel } from '@/hooks/useReviewPanel'
import { COMMON_ERROR_MESSAGES } from '@/constants/errors'

interface Props {
  data: ReviewPanelData
  viewMoreHref?: string
}

export default function ReviewPanel({ data, viewMoreHref }: Props) {
  const {
    photoOnly,
    setPhotoOnly,
    selectedRating,
    setSelectedRating,
    sortType,
    setSortType,
    deriveReviews,
  } = useReviewPanel()

  const { sortedReviews, photoReviewCount } = deriveReviews(data)

  return (
    <div className="flex flex-col gap-[3px] px-[15px] py-5">
      <ReviewFilter
        count={photoReviewCount}
        photoOnly={photoOnly}
        onPhotoOnlyChange={setPhotoOnly}
        selectedRating={selectedRating}
        onRatingChange={setSelectedRating}
        sortType={sortType}
        onSortTypeChange={setSortType}
      />
      <div className="flex flex-col divide-y divide-line">
        {sortedReviews.length === 0 ? (
          <div className="w-full py-4 text-sm leading-relaxed text-[#999999] text-center whitespace-pre-line">
            {selectedRating !== null
              ? `${selectedRating}점 리뷰가 없습니다.`
              : '아직 등록된 리뷰가 없습니다.\n첫 번째 리뷰를 남겨보세요!'}
          </div>
        ) : (
          sortedReviews.map((review) => <ReviewListItem key={review.id} review={review} />)
        )}
      </div>
      {selectedRating == null && data.totalReviewCount > 5 && viewMoreHref && (
        <div className="flex justify-center">
          <ViewMoreButton href={viewMoreHref} />
        </div>
      )}
    </div>
  )
}

export function ReviewPanelWrapper({
  data,
  isLoading,
  isError,
  viewMoreHref,
}: {
  data: ReviewPanelData | null | undefined
  isLoading: boolean
  isError: boolean
  viewMoreHref?: string
}) {
  if (isLoading) return <ReviewPanelSkeleton />
  if (isError || !data) return <FetchErrorState message={COMMON_ERROR_MESSAGES.API_FETCH_ERROR} />
  return <ReviewPanel data={data} viewMoreHref={viewMoreHref} />
}
