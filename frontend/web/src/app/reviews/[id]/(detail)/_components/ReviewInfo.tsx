import ReviewAuthorInfo from '@/components/reviews/ReviewAuthorInfo'
import ReviewImageGallery from '@/components/reviews/ReviewImageGallery'
import TextContent from '@/components/ui/TextContent'
import { ReactNode } from 'react'
import ReviewActions from './ReviewActions'
import ReviewTagList from './ReviewTagList'

interface Props {
  memberId: number
  memberProfileImageUrl: string | null
  memberNickname: string
  createdAt: string
  imageUrls: string[]
  content: string
  tagNames: string[]
  id: number
  reviewOption: ReactNode
  reviewLike: ReactNode
}

export default async function ReviewInfo({
  memberId,
  memberProfileImageUrl,
  memberNickname,
  createdAt,
  imageUrls,
  content,
  tagNames,
  id,
  reviewOption,
  reviewLike,
}: Props) {
  return (
    <>
      <div className="flex justify-between">
        <ReviewAuthorInfo
          memberId={memberId}
          profileImageUrl={memberProfileImageUrl}
          nickname={memberNickname}
          createdAt={createdAt}
        />
        {reviewOption}
      </div>
      <div className="mt-[15px]">
        <ReviewImageGallery imageUrls={imageUrls} />
      </div>
      <div className="mt-5">
        <TextContent text={content} />
      </div>
      <div className="mt-5">
        <ReviewTagList tagNames={tagNames} />
      </div>
      <ReviewActions reviewLike={reviewLike} reviewId={id} />
    </>
  )
}
