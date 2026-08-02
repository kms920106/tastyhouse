import ReviewAuthorInfo from '@/components/reviews/ReviewAuthorInfo'
import ReviewImageGallery from '@/components/reviews/ReviewImageGallery'
import ReviewOptionDrawer from '@/components/reviews/ReviewOptionDrawer'
import ClampedText from '@/components/ui/ClampedText'
import { PAGE_PATHS } from '@/lib/paths'

type LatestReviewItem = {
  id: number
  imageUrls: string[]
  content: string
  memberId: number
  memberNickname: string
  memberProfileImageUrl: string | null
  likeCount: number
  commentCount: number
  createdAt: string
}

interface Props {
  review: LatestReviewItem
}

export default function LatestReviewListItem({ review }: Props) {
  const {
    id,
    imageUrls,
    content,
    memberId,
    memberNickname,
    memberProfileImageUrl,
    likeCount,
    commentCount,
    createdAt,
  } = review

  return (
    <div className="px-[15px] pt-3 pb-[30px] bg-white">
      <div className="flex justify-between">
        <ReviewAuthorInfo
          memberId={memberId}
          profileImageUrl={memberProfileImageUrl}
          nickname={memberNickname}
          createdAt={createdAt}
        />
        <ReviewOptionDrawer
          reviewId={id}
          memberId={memberId}
          memberNickname={memberNickname}
          content={content}
        />
      </div>
      {imageUrls.length > 0 && (
        <div className="mt-[15px]">
          <ReviewImageGallery imageUrls={imageUrls} />
        </div>
      )}
      <div className="mt-5">
        <ClampedText text={content} href={PAGE_PATHS.REVIEW_DETAIL(id)} />
      </div>
      <div className="flex gap-4 mt-3.5">
        <span className="text-xs leading-[12px] text-[#aaaaaa]">좋아요 {likeCount}개</span>
        <span className="text-xs leading-[12px] text-[#aaaaaa]">댓글 {commentCount}개</span>
      </div>
    </div>
  )
}
