import type { EventStatus } from './event.types'

export interface EventListQuery {
  status: EventStatus
  page: number
  size: number
}

export interface EventListItemResponse {
  id: number
  name: string
  thumbnailImageUrl: string
  startAt: string
  endAt: string
}

export interface EventDetailResponse {
  bannerImageUrl: string
}

export interface EventAnnouncementListItemResponse {
  id: number
  name: string
  content: string
  announcedAt: string
}
