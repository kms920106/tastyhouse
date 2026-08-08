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
  searchParams: Promise<{
    scheduledAt?: string
  }>
}

export default async function Page({ params, searchParams }: Props) {
  const [{ id, orderMethod }, { scheduledAt }, isLoggedIn] = await Promise.all([
    params,
    searchParams,
    getIsLoggedIn(),
  ])
  const shopId = Number(id)
  if (!isLoggedIn) {
    redirect(PAGE_PATHS.AUTH_LOGIN)
  }

  // orderMethod 유효성은 [orderMethod]/layout.tsx에서 검증·redirect하므로 여기서는 타입만 좁힌다.
  const resolvedOrderMethod = parseOrderMethodSlug(orderMethod)
  if (!resolvedOrderMethod) {
    redirect(PAGE_PATHS.ORDER_METHOD(shopId))
  }

  // 장바구니에서 고른 수령 예약 시각. 유효성은 체크아웃이 슬롯을 재조회해 대조하고 서버도
  // 재계산하므로, 여기서는 값을 그대로 넘기기만 한다.
  return (
    <ShopOrderCheckoutPage
      shopId={shopId}
      orderMethod={resolvedOrderMethod}
      scheduledAt={scheduledAt ?? null}
    />
  )
}
