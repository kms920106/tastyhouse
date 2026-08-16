import type { Metadata } from 'next'
import { MENU_REVIEW_COPY } from '@/domains/menu-review'
import OrderMenuReviewPage from './_components/OrderMenuReviewPage'

export const metadata: Metadata = {
  title: MENU_REVIEW_COPY.SECTION_TITLE,
}

interface Props {
  params: Promise<{ id: string }>
}

export default async function Page({ params }: Props) {
  const { id } = await params

  return <OrderMenuReviewPage orderId={Number(id)} />
}
