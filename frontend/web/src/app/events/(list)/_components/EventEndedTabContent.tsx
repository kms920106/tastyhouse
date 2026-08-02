'use client'

import type { Event } from '@/domains/event'
import { useEndedEvents } from '@/domains/event/event.hook'
import { useIntersectionObserver } from '@/hooks/useIntersectionObserver'
import { formatDate } from '@/lib/date'
import { PAGE_PATHS } from '@/lib/paths'
import Image from 'next/image'
import Link from 'next/link'
import { useEffect } from 'react'
import { EventListSkeleton } from './EventListSkeleton'

export default function EventEndedTabContent() {
  const { data, fetchNextPage, hasNextPage, isFetchingNextPage, isLoading } = useEndedEvents()

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
    return <EventListSkeleton />
  }

  const events: Event[] = data?.pages.flatMap((page) => page.data ?? []) ?? []

  return (
    <>
      <div className="flex flex-col gap-2.5">
        {events.map((event) => (
          <Link key={event.id} href={PAGE_PATHS.EVENT_DETAIL(event.id)} className="cursor-pointer">
            <div className="relative w-full aspect-[2/1]">
              <Image
                src={event.thumbnailImageUrl}
                alt={event.name}
                fill
                className="object-cover"
                onError={(e) => {
                  const target = e.target as HTMLImageElement
                  target.style.display = 'none'
                }}
              />
            </div>
            <div className="px-[15px] py-5">
              <h3 className="text-base leading-[16px]">{event.name}</h3>
              <p className="mt-3 text-[13px] leading-[13px] font-light text-[#999999]">
                {formatDate(event.startAt, 'YYYY-MM-DD')} ~ {formatDate(event.endAt, 'YYYY-MM-DD')}
              </p>
            </div>
          </Link>
        ))}
        {isFetchingNextPage && <EventListSkeleton />}
      </div>
      <div ref={targetRef} className="h-1" aria-hidden="true" />
    </>
  )
}
