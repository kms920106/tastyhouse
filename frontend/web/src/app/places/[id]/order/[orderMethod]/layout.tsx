import { parseOrderMethodSlug } from '@/domains/order'
import { PAGE_PATHS } from '@/lib/paths'
import { redirect } from 'next/navigation'

interface Props {
  children: React.ReactNode
  params: Promise<{
    id: string
    orderMethod: string
  }>
}

export default async function Layout({ children, params }: Props) {
  const { id, orderMethod } = await params

  const parsed = parseOrderMethodSlug(orderMethod)
  if (!parsed || parsed === 'RESERVATION') {
    redirect(PAGE_PATHS.ORDER_METHOD(Number(id)))
  }

  return <>{children}</>
}
