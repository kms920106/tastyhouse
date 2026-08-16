'use server'

import { notificationRepository } from '@/domains/notification/notification.repository'
import { PAGE_PATHS } from '@/lib/paths'
import { revalidatePath } from 'next/cache'

export async function getNotifications({ page, size }: { page: number; size: number }) {
  return notificationRepository.getNotifications({ page, size })
}

export async function getUnreadNotificationCount() {
  return notificationRepository.getUnreadCount()
}

export async function readNotification(id: number) {
  const result = await notificationRepository.readNotification(id)

  if (!result.error) {
    revalidatePath(PAGE_PATHS.NOTIFICATIONS)
  }

  return result
}

export async function readAllNotifications() {
  const result = await notificationRepository.readAllNotifications()

  if (!result.error) {
    revalidatePath(PAGE_PATHS.NOTIFICATIONS)
  }

  return result
}
