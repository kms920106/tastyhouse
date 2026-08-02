import BorderedSection from '@/components/ui/BorderedSection'
import ProductReviewList from '@/components/products/ProductReviewList'
import ProductReviewStatistic from '@/components/products/ProductReviewStatistic'

interface Props {
  productId: number
}

export default function ShopOrderMenuDetailReviewTabContent({ productId }: Props) {
  return (
    <>
      <BorderedSection>
        <ProductReviewStatistic productId={productId} />
      </BorderedSection>
      <BorderedSection>
        <ProductReviewList productId={productId} />
      </BorderedSection>
    </>
  )
}
