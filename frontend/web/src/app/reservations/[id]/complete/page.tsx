import { getIsLoggedIn } from '@/lib/auth-config'
import { PAGE_PATHS } from '@/lib/paths'
import { notFound, redirect } from 'next/navigation'
import ReservationCompletePage from './_components/ReservationCompletePage'

interface Props {
  params: Promise<{ id: string }>
}

export default async function Page({ params }: Props) {
  const [{ id }, isLoggedIn] = await Promise.all([params, getIsLoggedIn()])

  if (!isLoggedIn) {
    redirect(PAGE_PATHS.AUTH_LOGIN)
  }

  const reservationId = Number(id)

  if (!reservationId) {
    notFound()
  }

  return <ReservationCompletePage reservationId={reservationId} />
}
