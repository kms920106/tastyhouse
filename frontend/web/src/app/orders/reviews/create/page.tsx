import OrderReviewCreatePage from './_components/OrderReviewCreatePage'

interface Props {
  searchParams: Promise<{
    orderProductId?: string
  }>
}

export default async function Page({ searchParams }: Props) {
  const { orderProductId } = await searchParams

  return <OrderReviewCreatePage orderProductId={Number(orderProductId)} />
}
