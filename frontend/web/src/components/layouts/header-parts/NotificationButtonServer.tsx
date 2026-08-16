import { notificationRepository } from '@/domains/notification/notification.repository'
import { getIsLoggedIn } from '@/lib/auth-config'
import NotificationButton from './NotificationButton'

/**
 * 헤더 알림 버튼의 데이터 페칭 담당 Server Component.
 *
 * 비로그인이면 알림함 자체가 의미가 없으므로 아무것도 렌더하지 않는다.
 * 조회 실패는 배지 없이(0건) 떨어뜨린다 — 배지 하나 때문에 헤더가 깨지면 안 된다.
 */
export default async function NotificationButtonServer() {
  const isLoggedIn = await getIsLoggedIn()

  if (!isLoggedIn) return null

  const { data } = await notificationRepository.getUnreadCount()

  return <NotificationButton unreadCount={data ?? 0} />
}
