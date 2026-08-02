import { getIsLoggedIn } from '@/lib/auth-config'
import { redirect } from 'next/navigation'
import OrderDetailPage from './_components/OrderDetailPage'
import { PAGE_PATHS } from '@/lib/paths'

interface Props {
  params: Promise<{
    id: string
  }>
}

export default async function Page({ params }: Props) {
  const [{ id }, isLoggedIn] = await Promise.all([params, getIsLoggedIn()])
  const orderId = Number(id)
  if (!isLoggedIn) {
    redirect(PAGE_PATHS.AUTH_LOGIN)
  }

  return <OrderDetailPage orderId={orderId} />
}
