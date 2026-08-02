import ReviewAuthorInfo from '@/components/reviews/ReviewAuthorInfo'
import ReviewImageGallery from '@/components/reviews/ReviewImageGallery'
import ReviewRatingDetail from '@/components/reviews/ReviewRatingDetail'
import BorderedSection from '@/components/ui/BorderedSection'
import Rating from '@/components/ui/Rating'
import TextContent from '@/components/ui/TextContent'
import { PAGE_PATHS } from '@/lib/paths'
import Link from 'next/link'
import ReviewTagList from '../../(detail)/_components/ReviewTagList'

interface Props {
  productId: number | null
  productName: string
  totalRating: number
  tasteRating: number
  amountRating: number
  priceRating: number
  atmosphereRating: number
  kindnessRating: number
  hygieneRating: number
  willRevisit: boolean
  memberId: number
  memberNickname: string
  memberProfileImageUrl: string | null
  createdAt: string
  content: string
  imageUrls: string[]
  tagNames: string[]
}

export default function ReviewDetailProductInfo({
  productId,
  productName,
  totalRating,
  tasteRating,
  amountRating,
  priceRating,
  atmosphereRating,
  kindnessRating,
  hygieneRating,
  willRevisit,
  memberId,
  memberNickname,
  memberProfileImageUrl,
  createdAt,
  content,
  imageUrls,
  tagNames,
}: Props) {
  return (
    <BorderedSection>
      <div className="px-[15px]">
        <div className="py-5 border-b border-line box-border">
          <ReviewRatingDetail
            averageAtmosphereRating={atmosphereRating}
            averageKindnessRating={kindnessRating}
            averageTasteRating={tasteRating}
            averageAmountRating={amountRating}
            averageHygieneRating={hygieneRating}
            averagePriceRating={priceRating}
            willRevisitPercentage={willRevisit ? 100 : 0}
          />
        </div>
        <div className="py-5">
          <div className="flex justify-between">
            <ReviewAuthorInfo
              memberId={memberId}
              profileImageUrl={memberProfileImageUrl}
              nickname={memberNickname}
              createdAt={createdAt}
            />
            <Rating as="p" value={totalRating} />
          </div>
          {productId && (
            <div className="mt-[25px]">
              <Link
                href={PAGE_PATHS.PRODUCT_DETAIL(productId)}
                className="block text-sm leading-[14px] text-[#999999]"
              >
                [선택] {productName}
              </Link>
            </div>
          )}
          <div className="mt-[15px]">
            <TextContent text={content} />
          </div>
          <div className="mt-5">
            <ReviewImageGallery imageUrls={imageUrls} />
          </div>
          <div className="mt-5">
            <ReviewTagList tagNames={tagNames} />
          </div>
        </div>
      </div>
    </BorderedSection>
  )
}
