'use client'

import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/shadcn/tabs'
import Icon from '@/components/ui/Icon'
import MemberDetailReviewTabContent from './MemberDetailReviewTabContent'

export type MemberDetailTab = 'reviews'

interface Props {
  memberId: number
  tab: MemberDetailTab
}

export default function MemberDetailTabs({ memberId, tab }: Props) {
  return (
    <div className="flex-1 flex flex-col border-t border-line">
      <Tabs value={tab} className="gap-0 flex flex-col flex-1">
        <TabsList className="sticky top-0 w-full h-[50px] rounded-none bg-white z-40 p-0 border-0 shadow-none">
          <TabsTrigger
            value="reviews"
            className="flex-1 h-full rounded-none border-0 border-b border-line shadow-none cursor-pointer data-[state=active]:shadow-none data-[state=active]:border-b-[1.5px] data-[state=active]:border-main"
          >
            <Icon name="mypage/review-on" />
          </TabsTrigger>
        </TabsList>
        <TabsContent value="reviews" className="mt-0 flex flex-col flex-1 bg-[#f9f9f9]">
          <MemberDetailReviewTabContent memberId={memberId} />
        </TabsContent>
      </Tabs>
    </div>
  )
}
