import ReviewOptionButton from '@/components/reviews/ReviewOptionButton'
import { Suspense } from 'react'
import ReviewInfo from './ReviewInfo'
import ReviewLikeButton from './ReviewLikeButton'
import ReviewLikeButtonServer from './ReviewLikeButtonServer'
import ReviewOptionDrawerServer from './ReviewOptionDrawerServer'

interface Props {
  reviewId: number
  memberId: number
  memberNickname: string
  memberProfileImageUrl: string | null
  createdAt: string
  imageUrls: string[]
  content: string
  tagNames: string[]
  isLoggedIn: boolean
}

export default function ReviewInfoSection({
  reviewId,
  memberId,
  memberNickname,
  memberProfileImageUrl,
  createdAt,
  imageUrls,
  content,
  tagNames,
  isLoggedIn,
}: Props) {

  return (
    <section className="px-[15px] pt-5 pb-8 border-b border-line box-border">
      <ReviewInfo
        memberId={memberId}
        memberProfileImageUrl={memberProfileImageUrl}
        memberNickname={memberNickname}
        createdAt={createdAt}
        imageUrls={imageUrls}
        content={content}
        tagNames={tagNames}
        id={reviewId}
        reviewLike={
          <Suspense fallback={<ReviewLikeButton isLiked={false} disabled={true} />}>
            <ReviewLikeButtonServer reviewId={reviewId} />
          </Suspense>
        }
        reviewOption={
          <Suspense fallback={<ReviewOptionButton disabled={true} />}>
            <ReviewOptionDrawerServer
              reviewId={reviewId}
              memberId={memberId}
              memberNickname={memberNickname}
              content={content}
              isLoggedIn={isLoggedIn}
            />
          </Suspense>
        }
      />
    </section>
  )
}
