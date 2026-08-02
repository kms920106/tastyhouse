import StickyFooter from '@/components/ui/StickyFooter'
import { Suspense } from 'react'
import ReviewCommentInput from './ReviewCommentInput'
import { ReviewCommentInputSkeleton } from './ReviewCommentInputSkeleton'

interface Props {
  isLoggedIn: boolean
  reviewId: number
}

export default function ReviewCommentInputSection({ isLoggedIn, reviewId }: Props) {
  return (
    <StickyFooter>
      <div className="p-[15px] flex items-center gap-[17px]">
        <Suspense fallback={<ReviewCommentInputSkeleton />}>
          <ReviewCommentInput isLoggedIn={isLoggedIn} reviewId={reviewId} />
        </Suspense>
      </div>
    </StickyFooter>
  )
}
