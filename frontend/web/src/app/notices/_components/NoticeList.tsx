'use client'

import { useIntersectionObserver } from '@/hooks/useIntersectionObserver'
import { useNoticeList } from '@/domains/notice/notice.hook'
import { Accordion } from '@/components/ui/shadcn/accordion'
import { useEffect } from 'react'
import { NoticeListSkeleton } from './NoticeListSkeleton'
import { NoticeListItem } from './NoticeListItem'

export default function NoticeList() {
  const { data, fetchNextPage, hasNextPage, isFetchingNextPage, isLoading } = useNoticeList()

  const { targetRef, isIntersecting, resetIntersecting } = useIntersectionObserver({
    threshold: 0.1,
    rootMargin: '100px',
    enabled: hasNextPage && !isFetchingNextPage,
  })

  useEffect(() => {
    if (isIntersecting && hasNextPage && !isFetchingNextPage) {
      resetIntersecting()
      fetchNextPage()
    }
  }, [isIntersecting, hasNextPage, isFetchingNextPage, fetchNextPage, resetIntersecting])

  if (isLoading) {
    return <NoticeListSkeleton />
  }

  const notices = data?.pages.flatMap((page) => page.data ?? []) ?? []

  return (
    <>
      <Accordion type="multiple" className="w-full">
        {notices.map((notice, index) => (
          <NoticeListItem key={notice.id} notice={notice} isFirst={index === 0} />
        ))}
        {isFetchingNextPage && <NoticeListSkeleton />}
      </Accordion>
      <div ref={targetRef} className="h-1" aria-hidden="true" />
    </>
  )
}
