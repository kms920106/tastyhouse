import { ProductHeaderSkeleton } from '@/components/products/ProductHeaderSkeleton'
import ProductInfoContent from '@/components/products/ProductInfoContent'
import { ProductInfoSkeleton } from '@/components/products/ProductInfoSkeleton'
import BorderedSection from '@/components/ui/BorderedSection'
import { ImageGallerySkeleton } from '@/components/ui/ImageGallerySkeleton'
import SectionStack from '@/components/ui/SectionStack'
import { Suspense } from 'react'
import ShopProductDetailBannerContent from './ShopProductDetailBannerContent'
import ShopProductDetailHeader from './ShopProductDetailHeader'
import ShopProductDetailOrderFooter from './ShopProductDetailOrderFooter'
import ShopProductDetailReviewCountServer from './ShopProductDetailReviewCountServer'
import { ShopProductDetailReviewCountSkeleton } from './ShopProductDetailReviewCountSkeleton'

interface Props {
  shopId: number
  productId: number
}

export default function ShopProductDetailPage({ shopId, productId }: Props) {
  return (
    <>
      <Suspense fallback={<ProductHeaderSkeleton />}>
        <ShopProductDetailHeader productId={productId} />
      </Suspense>
      <SectionStack>
        <BorderedSection>
          <Suspense fallback={<ImageGallerySkeleton />}>
            <ShopProductDetailBannerContent productId={productId} />
          </Suspense>
          <Suspense fallback={<ProductInfoSkeleton />}>
            <ProductInfoContent productId={productId} />
          </Suspense>
        </BorderedSection>
        <BorderedSection>
          <Suspense fallback={<ShopProductDetailReviewCountSkeleton />}>
            <ShopProductDetailReviewCountServer productId={productId} />
          </Suspense>
        </BorderedSection>
      </SectionStack>
      <ShopProductDetailOrderFooter shopId={shopId} />
    </>
  )
}
