import ReviewAuthorInfo from '@/components/reviews/ReviewAuthorInfo'
import ReviewImageGallery from '@/components/reviews/ReviewImageGallery'
import ReviewRatingDetail from '@/components/reviews/ReviewRatingDetail'
import BorderedSection from '@/components/ui/BorderedSection'
import FetchErrorState from '@/components/ui/FetchErrorState'
import Rating from '@/components/ui/Rating'
import SectionStack from '@/components/ui/SectionStack'
import TextContent from '@/components/ui/TextContent'
import { COMMON_ERROR_MESSAGES } from '@/constants/errors'
import { reviewRepository } from '@/domains/review/review.repository'
import { formatNumber } from '@/lib/number'
import { PAGE_PATHS } from '@/lib/paths'
import Image from 'next/image'
import Link from 'next/link'
import ReviewTagList from '../../(detail)/_components/ReviewTagList'

function Layout({ children }: { children: React.ReactNode }) {
  return <section>{children}</section>
}

interface Props {
  reviewId: number
}

export default async function ReviewProductInfoSection({ reviewId }: Props) {
  const { error, data } = await reviewRepository.getReviewProductDetail(reviewId)

  if (error) {
    return (
      <Layout>
        <div className="relative -top-[55px] flex items-center justify-center min-h-screen">
          <FetchErrorState message={COMMON_ERROR_MESSAGES.API_FETCH_ERROR} />
        </div>
      </Layout>
    )
  }

  if (!data) {
    return (
      <Layout>
        <div className="relative -top-[55px] flex items-center justify-center min-h-screen">
          <FetchErrorState message={COMMON_ERROR_MESSAGES.FETCH_ERROR('리뷰')} />
        </div>
      </Layout>
    )
  }

  const {
    productId,
    productName,
    productImageUrl,
    productPrice,
    content,
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
    imageUrls,
    tagNames,
  } = data

  return (
    <Layout>
      <SectionStack>
        {productId && (
          <>
            <BorderedSection>
              <div className="px-[15px] py-5 flex items-center gap-4">
                <div className="relative w-[50px] h-[50px] flex-shrink-0 overflow-hidden">
                  <Image
                    src={productImageUrl}
                    alt={productName}
                    fill
                    className="object-cover"
                    sizes="50px"
                  />
                </div>
                <div className="flex-1 flex flex-col min-w-0">
                  <h3 className="text-sm leading-[14px] truncate">{productName}</h3>
                  <span className="mt-2.5 text-sm leading-[14px]">
                    {formatNumber(productPrice)}원
                  </span>
                </div>
              </div>
            </BorderedSection>
          </>
        )}
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
      </SectionStack>
    </Layout>
  )
}
