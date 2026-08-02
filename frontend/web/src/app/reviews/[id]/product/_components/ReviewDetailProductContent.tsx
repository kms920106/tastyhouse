import SectionStack from '@/components/ui/SectionStack'
import ReviewDetailProductCard from './ReviewDetailProductCard'
import ReviewDetailProductInfo from './ReviewDetailProductInfo'

interface Props {
  productId: number | null
  productName: string
  productImageUrl: string | null
  productPrice: number
  content: string
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
  imageUrls: string[]
  tagNames: string[]
}

export default function ReviewDetailProductContent({
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
}: Props) {
  return (
    <SectionStack>
      {productId && (
        <ReviewDetailProductCard
          productImageUrl={productImageUrl}
          productName={productName}
          productPrice={productPrice}
        />
      )}
      <ReviewDetailProductInfo
        productId={productId}
        productName={productName}
        totalRating={totalRating}
        tasteRating={tasteRating}
        amountRating={amountRating}
        priceRating={priceRating}
        atmosphereRating={atmosphereRating}
        kindnessRating={kindnessRating}
        hygieneRating={hygieneRating}
        willRevisit={willRevisit}
        memberId={memberId}
        memberNickname={memberNickname}
        memberProfileImageUrl={memberProfileImageUrl}
        createdAt={createdAt}
        content={content}
        imageUrls={imageUrls}
        tagNames={tagNames}
      />
    </SectionStack>
  )
}
