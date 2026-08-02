'use client'

import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/shadcn/tabs'
import { useTabNavigation } from '@/hooks/useTabNavigation'
import ReviewAllTabContent from './ReviewAllTabContent'
import ReviewFollowingTabContent from './ReviewFollowingTabContent'

export type ReviewTab = 'all' | 'following'

const TABS: { label: string; value: ReviewTab }[] = [
  { label: '전체', value: 'all' },
  { label: '팔로잉', value: 'following' },
]


interface Props {
  tab: ReviewTab
}

export default function ReviewTabs({ tab }: Props) {
  const { handleTabChange } = useTabNavigation()

  const handleTabChangeWithScroll = (value: string) => {
    handleTabChange(value)
    window.scrollTo({ top: 0, behavior: 'instant' })
  }

  return (
    <Tabs value={tab} onValueChange={handleTabChangeWithScroll} className="gap-0">
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
        <ReviewAllTabContent />
      </TabsContent>
      <TabsContent value="following" className="mt-0">
        <ReviewFollowingTabContent />
      </TabsContent>
    </Tabs>
  )
}
