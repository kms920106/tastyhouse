'use client'

import { getEventAnnouncementList, getEventList } from '@/actions/event'
import { useInfiniteQuery } from '@tanstack/react-query'

const PAGE_SIZE = 10

export const eventQueryKeys = {
  active: ['events', 'ACTIVE'] as const,
  ended: ['events', 'ENDED'] as const,
  announcements: ['events', 'announcements'] as const,
}

export function useActiveEvents() {
  return useInfiniteQuery({
    queryKey: eventQueryKeys.active,
    queryFn: async ({ pageParam }) => {
      const response = await getEventList({ status: 'ACTIVE', page: pageParam, size: PAGE_SIZE })
      if (!response.data) throw new Error('응답 데이터가 없습니다.')
      return response
    },
    initialPageParam: 0,
    getNextPageParam: (lastPage) => {
      if (!lastPage.pagination) return undefined
      const { page, totalPages } = lastPage.pagination
      return page + 1 < totalPages ? page + 1 : undefined
    },
  })
}

export function useEndedEvents() {
  return useInfiniteQuery({
    queryKey: eventQueryKeys.ended,
    queryFn: async ({ pageParam }) => {
      const response = await getEventList({ status: 'ENDED', page: pageParam, size: PAGE_SIZE })
      if (!response.data) throw new Error('응답 데이터가 없습니다.')
      return response
    },
    initialPageParam: 0,
    getNextPageParam: (lastPage) => {
      if (!lastPage.pagination) return undefined
      const { page, totalPages } = lastPage.pagination
      return page + 1 < totalPages ? page + 1 : undefined
    },
  })
}

export function useEventAnnouncements() {
  return useInfiniteQuery({
    queryKey: eventQueryKeys.announcements,
    queryFn: async ({ pageParam }) => {
      const response = await getEventAnnouncementList({ page: pageParam, size: PAGE_SIZE })
      if (!response.data) throw new Error('응답 데이터가 없습니다.')
      return response
    },
    initialPageParam: 0,
    getNextPageParam: (lastPage) => {
      if (!lastPage.pagination) return undefined
      const { page, totalPages } = lastPage.pagination
      return page + 1 < totalPages ? page + 1 : undefined
    },
  })
}
