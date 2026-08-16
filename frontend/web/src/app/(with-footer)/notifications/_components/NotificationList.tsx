'use client'

import { readAllNotifications, readNotification } from '@/actions/notification'
import TimeAgo from '@/components/reviews/TimeAgo'
import { toast } from '@/components/ui/AppToaster'
import { NOTIFICATION_COPY } from '@/domains/notification'
import type { NotificationItem } from '@/domains/notification'
import { PAGE_PATHS } from '@/lib/paths'
import { useRouter } from 'next/navigation'
import { useTransition } from 'react'

interface Props {
  notifications: NotificationItem[]
}

/** 알림의 이동 대상 경로. 이동할 곳이 없으면 null. */
function resolveTargetHref(notification: NotificationItem): string | null {
  if (notification.targetType === 'REVIEW' && notification.targetId !== null) {
    return PAGE_PATHS.REVIEW_DETAIL(notification.targetId)
  }
  return null
}

export default function NotificationList({ notifications }: Props) {
  const router = useRouter()
  const [isPending, startTransition] = useTransition()

  const hasUnread = notifications.some((notification) => !notification.read)

  const handleItemClick = (notification: NotificationItem) => {
    const href = resolveTargetHref(notification)

    startTransition(async () => {
      // 이미 읽은 알림은 다시 호출하지 않는다 — 서버가 멱등이지만 불필요한 왕복을 줄인다.
      if (!notification.read) {
        await readNotification(notification.id)
      }
      if (href) {
        router.push(href)
      }
    })
  }

  const handleReadAll = () => {
    startTransition(async () => {
      const { error } = await readAllNotifications()

      if (error) {
        toast(NOTIFICATION_COPY.READ_ALL_FAILED)
        return
      }

      toast(NOTIFICATION_COPY.READ_ALL_SUCCESS)
      router.refresh()
    })
  }

  if (notifications.length === 0) {
    return (
      <div className="w-full py-20 text-sm leading-relaxed text-[#999999] text-center">
        {NOTIFICATION_COPY.EMPTY}
      </div>
    )
  }

  return (
    <>
      {hasUnread && (
        <div className="flex justify-end px-[15px] py-2.5">
          <button
            type="button"
            onClick={handleReadAll}
            disabled={isPending}
            className="text-xs leading-[12px] text-[#666666] underline cursor-pointer disabled:opacity-50"
          >
            {NOTIFICATION_COPY.READ_ALL}
          </button>
        </div>
      )}
      <ul className="flex flex-col divide-y divide-line border-t border-line">
        {notifications.map((notification) => (
          <li key={notification.id}>
            <button
              type="button"
              onClick={() => handleItemClick(notification)}
              disabled={isPending}
              className={`flex w-full flex-col items-start gap-1.5 px-[15px] py-4 text-left cursor-pointer disabled:opacity-50 ${
                notification.read ? 'bg-white' : 'bg-[#fafafa]'
              }`}
            >
              <div className="flex w-full items-center gap-1.5">
                {!notification.read && (
                  <span aria-hidden className="size-1.5 shrink-0 rounded-full bg-main" />
                )}
                <p className="text-sm leading-[14px] font-bold text-foreground">
                  {notification.title}
                </p>
              </div>
              <p className="text-sm leading-[20px] whitespace-pre-line text-foreground/80">
                {notification.body}
              </p>
              <TimeAgo date={notification.createdAt} />
            </button>
          </li>
        ))}
      </ul>
    </>
  )
}
