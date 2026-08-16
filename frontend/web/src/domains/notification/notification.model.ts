import type { NotificationTargetType, NotificationType } from './notification.types'

/**
 * 알림함 목록 항목.
 *
 * `src/app/**`의 컴포넌트가 Props로 직접 참조하므로 dto가 아닌 model에 둔다.
 */
export interface NotificationItem {
  id: number
  type: NotificationType
  title: string
  body: string
  /** 이동 대상 유형. 이동 대상이 없으면 null */
  targetType: NotificationTargetType | null
  /** 이동 대상 식별자. 없으면 null */
  targetId: number | null
  read: boolean
  createdAt: string
}
