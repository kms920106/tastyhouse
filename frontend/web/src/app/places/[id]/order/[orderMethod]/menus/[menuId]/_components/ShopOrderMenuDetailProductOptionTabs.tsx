'use client'

import SectionStack from '@/components/ui/SectionStack'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/shadcn/tabs'
import { useTabNavigation } from '@/hooks/useTabNavigation'
import ShopOrderMenuDetailOptionTabContent from './ShopOrderMenuDetailOptionTabContent'
import ShopOrderMenuDetailReviewTabContent from './ShopOrderMenuDetailReviewTabContent'

export type ProductOrderMenuDetailTab = 'options' | 'reviews'

interface Props {
  productId: number
  shopId: number
  reviewCount: number
  tab: ProductOrderMenuDetailTab
}

export default function ShopOrderMenuDetailProductOptionTabs({
  productId,
  shopId,
  reviewCount,
  tab,
}: Props) {
  const { handleTabChange } = useTabNavigation()

  const handleTabChangeWithScroll = (value: string) => {
    handleTabChange(value)
    window.scrollTo({ top: 0, behavior: 'instant' })
  }

  const TABS: { label: string; value: ProductOrderMenuDetailTab }[] = [
    { label: '옵션', value: 'options' },
    { label: `리뷰 (${reviewCount})`, value: 'reviews' },
  ]

  return (
    <Tabs value={tab} onValueChange={handleTabChangeWithScroll} className="gap-0">
      <TabsList className="sticky top-0 w-full h-[50px] p-0 rounded-none bg-white z-40 border-0 shadow-none">
        {TABS.map(({ label, value }) => (
          <TabsTrigger
            key={value}
            value={value}
            className="flex-1 h-full text-sm leading-[14px] text-foreground/40 border-0 border-b border-line rounded-none shadow-none cursor-pointer data-[state=active]:text-black data-[state=active]:font-bold data-[state=active]:shadow-none data-[state=active]:border-b-[1.5px] data-[state=active]:border-black"
          >
            {label}
          </TabsTrigger>
        ))}
      </TabsList>
      <TabsContent value="options" className="mt-0 data-[state=inactive]:hidden" forceMount>
        <ShopOrderMenuDetailOptionTabContent productId={productId} shopId={shopId} />
      </TabsContent>
      <TabsContent value="reviews" className="mt-0">
        <SectionStack>
          <ShopOrderMenuDetailReviewTabContent productId={productId} />
        </SectionStack>
      </TabsContent>
    </Tabs>
  )
}
