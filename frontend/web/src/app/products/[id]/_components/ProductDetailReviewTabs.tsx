'use client'

import SectionStack from '@/components/ui/SectionStack'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/shadcn/tabs'
import BorderedSection from '@/components/ui/BorderedSection'
import ProductReviewStatistic from '@/components/products/ProductReviewStatistic'
import ProductReviewList from '@/components/products/ProductReviewList'

export type ProductDetailReviewTab = 'reviews'

interface Props {
  productId: number
  reviewCount: number
}

export default function ProductDetailReviewTabs({ productId, reviewCount }: Props) {
  return (
    <Tabs value="reviews" className="gap-0">
      <TabsList className="sticky top-0 w-full h-[50px] p-0 rounded-none bg-white z-40 border-0 shadow-none">
        <TabsTrigger
          value="reviews"
          className="flex-1 h-full text-sm leading-[14px] text-foreground/40 border-0 border-b border-line rounded-none shadow-none cursor-pointer data-[state=active]:text-black data-[state=active]:font-bold data-[state=active]:shadow-none data-[state=active]:border-b-[1.5px] data-[state=active]:border-black"
        >
          {`리뷰 (${reviewCount})`}
        </TabsTrigger>
      </TabsList>
      <TabsContent value="reviews" className="mt-0">
        <SectionStack>
          <BorderedSection>
            <ProductReviewStatistic productId={productId} />
          </BorderedSection>
          <BorderedSection>
            <ProductReviewList productId={productId} />
          </BorderedSection>
        </SectionStack>
      </TabsContent>
    </Tabs>
  )
}
