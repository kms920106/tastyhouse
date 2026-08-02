'use client'

import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/shadcn/tabs'
import { useTabNavigation } from '@/hooks/useTabNavigation'
import ShopDetailInfoTabContent from './ShopDetailInfoTabContent'
import ShopDetailMenuTabContent from './ShopDetailMenuTabContent'
import ShopDetailPhotoTabContent from './ShopDetailPhotoTabContent'
import ShopDetailReviewTabContent from './ShopDetailReviewTabContent'

export type ShopDetailTab = 'info' | 'menu' | 'photo' | 'review'

const TABS: { label: string; value: ShopDetailTab }[] = [
  { label: '정보', value: 'info' },
  { label: '메뉴', value: 'menu' },
  { label: '포토', value: 'photo' },
  { label: '리뷰', value: 'review' },
]

interface Props {
  shopId: number
  tab: ShopDetailTab
}

export default function ShopDetailTabs({ shopId, tab }: Props) {
  const { handleTabChange } = useTabNavigation()

  return (
    <Tabs value={tab} onValueChange={handleTabChange} className="gap-0 w-full">
      <div className="sticky top-[60px] bg-white z-40">
        <TabsList className="w-full h-auto p-0 bg-white border-0">
          {TABS.map(({ label, value }) => (
            <TabsTrigger
              key={value}
              value={value}
              className="flex-1 p-0 py-[18px] text-sm leading-[14px] text-foreground/40 border-b border-b-line! rounded-none shadow-none cursor-pointer data-[state=active]:text-black data-[state=active]:font-bold data-[state=active]:border-black data-[state=active]:border-t data-[state=active]:border-r data-[state=active]:border-l data-[state=active]:border-b-0 data-[state=active]:border-l-line data-[state=active]:border-r-line data-[state=active]:shadow-none"
            >
              {label}
            </TabsTrigger>
          ))}
        </TabsList>
      </div>
      <TabsContent value="info" className="mt-0">
        <ShopDetailInfoTabContent shopId={shopId} />
      </TabsContent>
      <TabsContent value="menu" className="mt-0">
        <ShopDetailMenuTabContent shopId={shopId} />
      </TabsContent>
      <TabsContent value="photo" className="mt-0">
        <ShopDetailPhotoTabContent shopId={shopId} />
      </TabsContent>
      <TabsContent value="review" className="mt-0">
        <ShopDetailReviewTabContent shopId={shopId} />
      </TabsContent>
    </Tabs>
  )
}
