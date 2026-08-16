import { NOTIFICATION_BADGE_MAX_COUNT } from '@/domains/notification'
import { PAGE_PATHS } from '@/lib/paths'
import { IoNotificationsOutline } from 'react-icons/io5'
import HeaderIconLink from './HeaderIconLink'

interface Props {
  /** 미읽음 개수. 0이면 배지를 숨긴다. */
  unreadCount: number
}

/**
 * 헤더 알림 아이콘 + 미읽음 배지.
 *
 * 폴링하지 않는다 — 페이지 이동 시 서버 렌더로 갱신되는 수준으로 둔다.
 */
export default function NotificationButton({ unreadCount }: Props) {
  const hasUnread = unreadCount > 0
  const badgeLabel =
    unreadCount > NOTIFICATION_BADGE_MAX_COUNT
      ? `${NOTIFICATION_BADGE_MAX_COUNT}+`
      : String(unreadCount)

  return (
    <HeaderIconLink href={PAGE_PATHS.NOTIFICATIONS} className="relative">
      <IoNotificationsOutline size={22} />
      {hasUnread && (
        <span className="absolute top-2.5 right-2.5 flex items-center justify-center min-w-[17px] h-[17px] px-1 rounded-full bg-red-500 text-[10px] leading-none font-bold text-white">
          {badgeLabel}
        </span>
      )}
    </HeaderIconLink>
  )
}
