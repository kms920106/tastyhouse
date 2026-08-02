import ReviewAuthorInfo from '@/components/reviews/ReviewAuthorInfo'
import ReviewImageGallery from '@/components/reviews/ReviewImageGallery'
import ClampedText from '@/components/ui/ClampedText'
import Rating from '@/components/ui/Rating'
import { PAGE_PATHS } from '@/lib/paths'

export interface ReviewListItemData {
  id: number
  memberId: number
  memberProfileImageUrl: string | null
  memberNickname: string
  createdAt: string
  totalRating: number
  productId: number | null
  productName: string | null
  content: string
  imageUrls: string[]
}

interface Props {
  review: ReviewListItemData
}

export default function ReviewListItem({ review }: Props) {
  return (
    <div className="py-5">
      <div className="flex justify-between">
        <ReviewAuthorInfo
          memberId={review.memberId}
          profileImageUrl={review.memberProfileImageUrl}
          nickname={review.memberNickname}
          createdAt={review.createdAt}
        />
        <Rating as="p" value={review.totalRating} />
      </div>
      {/* 상품 상세와 동일한 상품으로 인해 주석 처리 */}
      {/* {review.productId && (
        <div className="mt-[25px]">
          <Link
            href={PAGE_PATHS.PRODUCT_DETAIL(review.productId)}
            className="block text-sm leading-[14px] text-[#999999]"
          >
            [선택] {review.productName}
          </Link>
        </div>
      )} */}
      <div className="mt-[15px]">
        <ClampedText text={review.content} href={PAGE_PATHS.REVIEW_PRODUCT_DETAIL(review.id)} />
      </div>
      {review.imageUrls.length > 0 && (
        <div className="mt-5">
          <ReviewImageGallery imageUrls={review.imageUrls} />
        </div>
      )}
    </div>
  )
}
