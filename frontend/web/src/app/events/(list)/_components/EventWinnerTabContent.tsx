'use client'

import { useEventAnnouncements } from '@/domains/event/event.hook'
import { useIntersectionObserver } from '@/hooks/useIntersectionObserver'
import { formatDate } from '@/lib/date'
import { cn } from '@/lib/utils'
import * as AccordionPrimitive from '@radix-ui/react-accordion'
import { useEffect } from 'react'
import { AnnouncementListSkeleton } from './AnnouncementListSkeleton'

export default function EventWinnerTabContent() {
  const { data, fetchNextPage, hasNextPage, isFetchingNextPage, isLoading } = useEventAnnouncements()

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
    return <AnnouncementListSkeleton />
  }

  const events = data?.pages.flatMap((page) => page.data ?? []) ?? []

  return (
    <>
      <AccordionPrimitive.Root type="multiple">
        {events.map((event, index) => (
          <AccordionPrimitive.Item
            key={event.id}
            value={String(event.id)}
            className="border-b border-line last:border-b-0"
          >
            <AccordionPrimitive.Header>
              <AccordionPrimitive.Trigger
                className={cn(
                  'w-full flex items-start justify-between px-[16px] py-[18px] [&[data-state=open]>img]:rotate-180 group',
                  index === 0 && 'pt-0',
                )}
              >
                <div className="flex flex-col items-start gap-3">
                  <span className="text-sm leading-[14px]">{event.name}</span>
                  <span className="text-[13px] leading-[13px] text-[#999999] font-thin">
                    {formatDate(event.announcedAt, 'YYYY-MM-DD')}
                  </span>
                </div>
              </AccordionPrimitive.Trigger>
            </AccordionPrimitive.Header>
            <AccordionPrimitive.Content className="data-[state=closed]:animate-accordion-up data-[state=open]:animate-accordion-down overflow-hidden">
              <p className="text-[13px] leading-[22px] p-[25px] whitespace-pre-line bg-[#f9f9f9]">
                {event.content}
              </p>
            </AccordionPrimitive.Content>
          </AccordionPrimitive.Item>
        ))}
        {isFetchingNextPage && <AnnouncementListSkeleton />}
      </AccordionPrimitive.Root>
      <div ref={targetRef} className="h-1" aria-hidden="true" />
    </>
  )
}
