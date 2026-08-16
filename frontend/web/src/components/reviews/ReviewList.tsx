import EmptyState from '@/app/(with-footer)/(without-sidebar)/mypage/_components/EmptyState'
import ReviewThumbnail from '@/components/reviews/ReviewThumbnail'
import ViewMoreButton from '@/components/ui/ViewMoreButton'
import type { MyReviewListItemResponse } from '@/domains/member'
import { PAGE_PATHS } from '@/lib/paths'
import Link from 'next/link'

interface Props {
  reviews: MyReviewListItemResponse[]
  hasMoreReviews: boolean
  /**
   * '사장님만보기' 뱃지 노출 여부. 본인 리뷰 목록(마이페이지)에서만 켠다.
   * 타인 프로필은 서버가 사장님만보기 리뷰를 아예 내려주지 않으므로 뱃지를 켤 이유가 없다.
   */
  showOwnerOnlyBadge?: boolean
}

export default function ReviewList({
  reviews,
  hasMoreReviews,
  showOwnerOnlyBadge = false,
}: Props) {
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
            <Link
              key={review.id}
              href={PAGE_PATHS.REVIEW_DETAIL(review.id)}
              className="relative block"
            >
              <ReviewThumbnail imageUrl={review.imageUrl} priority={index === 0} />
              {showOwnerOnlyBadge && review.ownerOnly && (
                <span className="absolute left-1 top-1 rounded-[3px] bg-black/60 px-1.5 py-0.5 text-[10px] leading-[10px] text-white">
                  사장님만보기
                </span>
              )}
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
