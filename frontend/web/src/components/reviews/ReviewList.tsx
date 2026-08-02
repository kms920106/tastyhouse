import EmptyState from '@/app/(with-footer)/(without-sidebar)/mypage/_components/EmptyState'
import ReviewThumbnail from '@/components/reviews/ReviewThumbnail'
import ViewMoreButton from '@/components/ui/ViewMoreButton'
import type { MyReviewListItemResponse } from '@/domains/member'
import { PAGE_PATHS } from '@/lib/paths'
import Link from 'next/link'

interface Props {
  reviews: MyReviewListItemResponse[]
  hasMoreReviews: boolean
}

export default function ReviewList({ reviews, hasMoreReviews }: Props) {
  if (reviews.length === 0) {
    return (
      <>
        <EmptyState message="등록된 리뷰가 없습니다." />
        <div className="h-[70px]" />
      </>
    )
  }

  return (
    <>
      <div className="py-[1px]">
        <div className="grid grid-cols-3 gap-[1.5px]">
          {reviews.map((review, index) => (
            <Link key={review.id} href={PAGE_PATHS.REVIEW_DETAIL(review.id)}>
              <ReviewThumbnail imageUrl={review.imageUrl} priority={index === 0} />
            </Link>
          ))}
        </div>
        {hasMoreReviews && (
          <div className="flex justify-center py-5">
            <ViewMoreButton href={PAGE_PATHS.MY_REVIEWS} label="더 보러가기" />
          </div>
        )}
      </div>
      <div className="h-[70px]" />
    </>
  )
}
