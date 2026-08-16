import type { NotificationItem } from './notification.model'

/**
 * `GET /api/notifications/v1` 목록 항목.
 *
 * 서버 응답 필드가 도메인 모델과 1:1이라 model을 그대로 재사용한다.
 */
export type NotificationListItemResponse = NotificationItem
