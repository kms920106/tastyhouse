'use client'

import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/shadcn/tabs'
import { useTabNavigation } from '@/hooks/useTabNavigation'
import SearchResultMenuTabContent from './SearchResultMenuTabContent'
import SearchResultShopTabContent from './SearchResultShopTabContent'
import SearchResultReviewTabContent from './SearchResultReviewTabContent'
import SearchResultAllTabContent from './SearchResultAllTabContent'

export type SearchTab = 'all' | 'menu' | 'review' | 'place'

const TABS: { label: string; value: SearchTab }[] = [
  { label: '전체', value: 'all' },
  { label: '메뉴', value: 'menu' },
  { label: '리뷰', value: 'review' },
  { label: '플레이스', value: 'place' },
]

interface Props {
  tab: SearchTab
  query: string
  isLoggedIn: boolean
}

export default function SearchResultTabs({ tab, query, isLoggedIn }: Props) {
  const { handleTabChange } = useTabNavigation()

  return (
    <Tabs value={tab} onValueChange={handleTabChange} className="gap-0">
      <TabsList className="sticky top-0 w-full h-[50px] p-0 rounded-none bg-white z-40 border-0 shadow-none">
        {TABS.map(({ label, value }) => (
          <TabsTrigger
            key={value}
            value={value}
            className="flex-1 h-full text-sm leading-[14px] text-foreground/40 border-0 border-b border-line rounded-none shadow-none cursor-pointer data-[state=active]:text-main data-[state=active]:font-bold data-[state=active]:shadow-none data-[state=active]:border-b-[1.5px] data-[state=active]:border-main"
          >
            {label}
          </TabsTrigger>
        ))}
      </TabsList>
      <TabsContent value="all" className="mt-0">
        <SearchResultAllTabContent query={query} isLoggedIn={isLoggedIn} />
      </TabsContent>
      <TabsContent value="menu" className="mt-0">
        <SearchResultMenuTabContent query={query} />
      </TabsContent>
      <TabsContent value="review" className="mt-0">
        <SearchResultReviewTabContent query={query} />
      </TabsContent>
      <TabsContent value="place" className="mt-0">
        <SearchResultShopTabContent query={query} isLoggedIn={isLoggedIn} />
      </TabsContent>
    </Tabs>
  )
}
