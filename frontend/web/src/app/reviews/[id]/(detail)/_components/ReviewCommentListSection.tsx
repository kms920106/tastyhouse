import { Suspense } from 'react'
import ReviewCommentList from './ReviewCommentList'
import { ReviewCommentListSkeleton } from './ReviewCommentListSkeleton'

interface Props {
  reviewId: number
  isLoggedIn: boolean
}

export default function ReviewCommentListSection({ reviewId, isLoggedIn }: Props) {
  return (
    <section>
      <div className="px-[15px] py-5">
        <div className="space-y-[30px]">
          <Suspense fallback={<ReviewCommentListSkeleton />}>
            <ReviewCommentList reviewId={reviewId} isLoggedIn={isLoggedIn} />
          </Suspense>
        </div>
      </div>
    </section>
  )
}
