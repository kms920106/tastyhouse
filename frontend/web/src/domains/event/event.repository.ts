import 'server-only'

import { api } from '@/lib/api'
import { PaginationParams } from '@/types/common'
import {
  EventAnnouncementListItemResponse,
  EventDetailResponse,
  EventListItemResponse,
  EventListQuery,
} from './event.dto'

const ENDPOINT = '/api/event'

export const eventRepository = {
  // 이벤트 목록 조회
  async getEventList(params: EventListQuery) {
    return api.get<EventListItemResponse[]>(`${ENDPOINT}/v1/list`, { params })
  },
  // 이벤트 상세 조회
  async getEventDetail(eventId: number) {
    return api.get<EventDetailResponse>(`${ENDPOINT}/v1/${eventId}`)
  },
  // 당첨자 발표 목록 조회
  async getEventAnnouncementList(params: PaginationParams) {
    return api.get<EventAnnouncementListItemResponse[]>(`${ENDPOINT}/v1/announcements`, { params })
  },
}
