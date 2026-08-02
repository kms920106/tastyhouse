import { getIsLoggedIn } from '@/lib/auth-config'
import { PAGE_PATHS } from '@/lib/paths'
import type { Metadata } from 'next'
import { redirect } from 'next/navigation'
import ReservationListPage from './_components/ReservationListPage'

export const metadata: Metadata = {
  title: '예약 내역',
}

export default async function Page() {
  const isLoggedIn = await getIsLoggedIn()

  if (!isLoggedIn) {
    redirect(PAGE_PATHS.AUTH_LOGIN)
  }

  return <ReservationListPage />
}
