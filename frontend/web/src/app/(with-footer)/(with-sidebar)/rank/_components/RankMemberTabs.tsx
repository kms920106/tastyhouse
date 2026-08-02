'use client'

import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/shadcn/tabs'
import { useTabNavigation } from '@/hooks/useTabNavigation'
import { ReactNode } from 'react'

export type RankTab = 'all' | 'monthly'

const TABS: { label: string; value: RankTab }[] = [
  { label: '전체', value: 'all' },
  { label: '이번 달', value: 'monthly' },
]

interface Props {
  tab: RankTab
  scheduleArea: ReactNode
  infoButton: ReactNode
  allContent: ReactNode
  monthlyContent: ReactNode
}

export default function RankMemberTabs({
  tab,
  scheduleArea,
  infoButton,
  allContent,
  monthlyContent,
}: Props) {
  const { handleTabChange } = useTabNavigation()

  return (
    <Tabs value={tab} onValueChange={handleTabChange} className="gap-0">
      <div className="flex justify-between">
        <div className="flex gap-2.5">
          <TabsList className="flex items-start gap-3 p-0 bg-white border-0 shadow-none">
            {TABS.map(({ label, value }) => (
              <TabsTrigger
                key={value}
                value={value}
                className="p-0 !h-[18px] text-lg leading-[18px] font-bold text-foreground/50 data-[state=active]:text-black data-[state=active]:shadow-none cursor-pointer"
              >
                {label}
              </TabsTrigger>
            ))}
          </TabsList>
          {infoButton}
        </div>
        {scheduleArea}
      </div>
      <TabsContent value="all" className="mt-0">
        <div className="flex flex-col flex-1 gap-2.5 py-[25px]">{allContent}</div>
        <div className="h-[142px]" />
      </TabsContent>
      <TabsContent value="monthly" className="mt-0">
        <div className="flex flex-col flex-1 gap-2.5 py-[25px]">{monthlyContent}</div>
        <div className="h-[142px]" />
      </TabsContent>
    </Tabs>
  )
}
