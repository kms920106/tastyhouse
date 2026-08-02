'use client'

import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/shadcn/tabs'
import { useTabNavigation } from '@/hooks/useTabNavigation'
import Image from 'next/image'
import { useState } from 'react'
import MyPageBookmarkTabContent from './MyPageBookmarkTabContent'
import EmptyState from './EmptyState'
import MyPageOrderTabContent from './MyPageOrderTabContent'
import MyPageReviewTabContent from './MyPageReviewTabContent'

export type MyPageTab = 'reviews' | 'payments' | 'bookmarks'

const TABS: { label: string; value: MyPageTab; iconBase: string; width: number; height: number }[] =
  [
    { label: '리뷰', value: 'reviews', iconBase: 'icon-review', width: 22, height: 25 },
    { label: '결제', value: 'payments', iconBase: 'icon-order', width: 34, height: 25 },
    { label: '북마크', value: 'bookmarks', iconBase: 'icon-place-bookmark', width: 27, height: 25 },
  ]

interface Props {
  tab: MyPageTab
  isLoggedIn: boolean
}

export default function MyPageTabs({ tab, isLoggedIn }: Props) {
  const { handleTabChange } = useTabNavigation()
  const [currentTab, setCurrentTab] = useState<MyPageTab>(tab)

  const handleChange = (value: string) => {
    setCurrentTab(value as MyPageTab)
    handleTabChange(value)
  }

  return (
    <div className="flex-1 flex flex-col border-t border-line">
      <Tabs
        value={currentTab}
        onValueChange={handleChange}
        className="gap-0 flex flex-col flex-1"
      >
        <TabsList className="sticky top-0 w-full h-[50px] rounded-none bg-white z-40 p-0 border-0 shadow-none">
          {TABS.map(({ label, value, iconBase, width, height }) => (
            <TabsTrigger
              key={value}
              value={value}
              className="flex-1 h-full rounded-none border-0 border-b border-line shadow-none cursor-pointer data-[state=active]:shadow-none data-[state=active]:border-b-[1.5px] data-[state=active]:border-main"
            >
              <Image
                src={`/images/mypage/${iconBase}-${currentTab === value ? 'on' : 'off'}.png`}
                alt={label}
                width={width}
                height={height}
              />
            </TabsTrigger>
          ))}
        </TabsList>
        <TabsContent value="reviews" className="mt-0 flex flex-col flex-1 bg-[#f9f9f9]">
          {isLoggedIn ? (
            <MyPageReviewTabContent />
          ) : (
            <>
              <EmptyState message="로그인이 필요한 서비스입니다." />
              <div className="h-[70px]" />
            </>
          )}
        </TabsContent>
        <TabsContent value="payments" className="mt-0 flex flex-col flex-1 bg-[#f9f9f9]">
          {isLoggedIn ? (
            <MyPageOrderTabContent />
          ) : (
            <>
              <EmptyState message="로그인이 필요한 서비스입니다." />
              <div className="h-[70px]" />
            </>
          )}
        </TabsContent>
        <TabsContent value="bookmarks" className="mt-0 flex flex-col flex-1 bg-[#f9f9f9]">
          {isLoggedIn ? (
            <MyPageBookmarkTabContent />
          ) : (
            <>
              <EmptyState message="로그인이 필요한 서비스입니다." />
              <div className="h-[70px]" />
            </>
          )}
        </TabsContent>
      </Tabs>
    </div>
  )
}
