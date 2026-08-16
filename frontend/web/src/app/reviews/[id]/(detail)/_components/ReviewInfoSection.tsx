import ReviewOptionButton from '@/components/reviews/ReviewOptionButton'
import ReviewOwnerReply from '@/components/reviews/ReviewOwnerReply'
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
  shopName: string
  ownerReplyContent: string | null
  ownerReplyCreatedAt: string | null
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
  shopName,
  ownerReplyContent,
  ownerReplyCreatedAt,
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
      {/* 사장님 답변은 그 리뷰에 대한 가게의 공식 응답이므로 제3자 대화인 고객 댓글보다 위에 온다. */}
      {ownerReplyContent && ownerReplyCreatedAt && (
        <div className="mt-5">
          <ReviewOwnerReply
            shopName={shopName}
            content={ownerReplyContent}
            createdAt={ownerReplyCreatedAt}
          />
        </div>
      )}
    </section>
  )
}
