import { parseOrderMethodSlug } from '@/domains/order'
import { getIsLoggedIn } from '@/lib/auth-config'
import { PAGE_PATHS } from '@/lib/paths'
import { redirect } from 'next/navigation'
import ShopOrderCheckoutPage from './_components/ShopOrderCheckoutPage'

interface Props {
  params: Promise<{
    id: string
    orderMethod: string
  }>
}

export default async function Page({ params }: Props) {
  const [{ id, orderMethod }, isLoggedIn] = await Promise.all([params, getIsLoggedIn()])
  const shopId = Number(id)
  if (!isLoggedIn) {
    redirect(PAGE_PATHS.AUTH_LOGIN)
  }

  // orderMethod 유효성은 [orderMethod]/layout.tsx에서 검증·redirect하므로 여기서는 타입만 좁힌다.
  const resolvedOrderMethod = parseOrderMethodSlug(orderMethod)
  if (!resolvedOrderMethod) {
    redirect(PAGE_PATHS.ORDER_METHOD(shopId))
  }

  return <ShopOrderCheckoutPage shopId={shopId} orderMethod={resolvedOrderMethod} />
}
