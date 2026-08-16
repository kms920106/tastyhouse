import 'server-only'

import { api } from '@/lib/api'
import type { PaginationParams } from '@/types/common'

import type { NotificationListItemResponse } from './notification.dto'

const ENDPOINT = '/api/notifications'

export const notificationRepository = {
  // 내 알림 목록 조회 (최신순, 페이징)
  async getNotifications(params: PaginationParams) {
    return api.get<NotificationListItemResponse[], PaginationParams>(`${ENDPOINT}/v1`, { params })
  },
  // 미읽음 개수 조회 — 헤더 배지용
  async getUnreadCount() {
    return api.get<number>(`${ENDPOINT}/v1/unread-count`)
  },
  // 단건 읽음 처리 (멱등)
  async readNotification(id: number) {
    return api.put<void>(`${ENDPOINT}/v1/${id}/read`)
  },
  // 전체 읽음 처리
  async readAllNotifications() {
    return api.put<void>(`${ENDPOINT}/v1/read-all`)
  },
}
