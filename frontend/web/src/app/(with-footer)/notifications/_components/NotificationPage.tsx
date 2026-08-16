import FetchErrorState from '@/components/ui/FetchErrorState'
import { COMMON_ERROR_MESSAGES } from '@/constants/errors'
import { NOTIFICATION_PAGE_SIZE } from '@/domains/notification'
import { notificationRepository } from '@/domains/notification/notification.repository'
import { getIsLoggedIn } from '@/lib/auth-config'
import { PAGE_PATHS } from '@/lib/paths'
import { redirect } from 'next/navigation'
import NotificationHeader from './NotificationHeader'
import NotificationList from './NotificationList'

export default async function NotificationPage() {
  const isLoggedIn = await getIsLoggedIn()

  if (!isLoggedIn) {
    redirect(PAGE_PATHS.AUTH_LOGIN)
  }

  const { error, status, data } = await notificationRepository.getNotifications({
    page: 0,
    size: NOTIFICATION_PAGE_SIZE,
  })

  if (error && status === 401) {
    redirect(PAGE_PATHS.AUTH_LOGIN)
  }

  if (error) {
    return (
      <>
        <NotificationHeader />
        <FetchErrorState message={COMMON_ERROR_MESSAGES.API_FETCH_ERROR} />
      </>
    )
  }

  return (
    <>
      <NotificationHeader />
      <NotificationList notifications={data ?? []} />
    </>
  )
}
