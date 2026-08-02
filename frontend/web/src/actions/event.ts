'use server'

import { eventRepository } from '@/domains/event/event.repository'

export async function getEventList({
  status,
  page,
  size,
}: {
  status: 'ACTIVE' | 'ENDED'
  page: number
  size: number
}) {
  return eventRepository.getEventList({ status, page, size })
}

export async function getEventAnnouncementList({ page, size }: { page: number; size: number }) {
  return eventRepository.getEventAnnouncementList({ page, size })
}
