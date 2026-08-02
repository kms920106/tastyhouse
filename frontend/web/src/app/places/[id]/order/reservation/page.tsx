import { getIsLoggedIn } from '@/lib/auth-config'
import { PAGE_PATHS } from '@/lib/paths'
import { redirect } from 'next/navigation'
import ReservationPage from './_components/ReservationPage'

interface Props {
  params: Promise<{
    id: string
  }>
}

export default async function Page({ params }: Props) {
  const [{ id }, isLoggedIn] = await Promise.all([params, getIsLoggedIn()])

  const shopId = Number(id)

  if (!isLoggedIn) {
    redirect(PAGE_PATHS.AUTH_LOGIN)
  }

  return <ReservationPage shopId={shopId} />
}
