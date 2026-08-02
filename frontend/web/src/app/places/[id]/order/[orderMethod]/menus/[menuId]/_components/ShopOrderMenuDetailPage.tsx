import ProductInfoContent from '@/components/products/ProductInfoContent'
import { ProductInfoSkeleton } from '@/components/products/ProductInfoSkeleton'
import BorderedSection from '@/components/ui/BorderedSection'
import { ImageGallerySkeleton } from '@/components/ui/ImageGallerySkeleton'
import SectionStack from '@/components/ui/SectionStack'
import { Suspense } from 'react'
import ShopOrderMenuDetailBannerContent from './ShopOrderMenuDetailBannerContent'
import ShopOrderMenuDetailHeader from './ShopOrderMenuDetailHeader'
import { ShopOrderMenuDetailHeaderSkeleton } from './ShopOrderMenuDetailHeaderSkeleton'
import ShopOrderMenuDetailOptionSelectorServer from './ShopOrderMenuDetailOptionSelectorServer'
import { ShopOrderMenuDetailOptionSelectorSkeleton } from './ShopOrderMenuDetailOptionSelectorSkeleton'
import type { OrderMethodType } from '@/domains/order'
import type { ProductOrderMenuDetailTab } from './ShopOrderMenuDetailProductOptionTabs'

interface Props {
  shopId: number
  productId: number
  tab: ProductOrderMenuDetailTab
  orderMethod: OrderMethodType
}

export default function ShopOrderMenuDetailPage({ shopId, productId, tab, orderMethod }: Props) {
  return (
    <>
      <Suspense fallback={<ShopOrderMenuDetailHeaderSkeleton />}>
        <ShopOrderMenuDetailHeader
          shopId={shopId}
          productId={productId}
          orderMethod={orderMethod}
        />
      </Suspense>
      <SectionStack>
        <BorderedSection>
          <Suspense fallback={<ImageGallerySkeleton />}>
            <ShopOrderMenuDetailBannerContent productId={productId} />
          </Suspense>
          <Suspense fallback={<ProductInfoSkeleton />}>
            <ProductInfoContent productId={productId} />
          </Suspense>
        </BorderedSection>
        <BorderedSection>
          <Suspense fallback={<ShopOrderMenuDetailOptionSelectorSkeleton />}>
            <ShopOrderMenuDetailOptionSelectorServer
              productId={productId}
              shopId={shopId}
              tab={tab}
            />
          </Suspense>
        </BorderedSection>
      </SectionStack>
    </>
  )
}
