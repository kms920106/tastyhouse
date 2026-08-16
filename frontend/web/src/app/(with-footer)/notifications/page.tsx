import type { Metadata } from 'next'
import { NOTIFICATION_COPY } from '@/domains/notification'
import NotificationPage from './_components/NotificationPage'

export const metadata: Metadata = {
  title: NOTIFICATION_COPY.PAGE_TITLE,
}

export default function Page() {
  return <NotificationPage />
}
