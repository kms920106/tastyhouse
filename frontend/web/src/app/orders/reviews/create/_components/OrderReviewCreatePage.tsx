import OrderReviewCreateContent from './OrderReviewCreateContent'
import OrderReviewCreateHeader from './OrderReviewCreateHeader'

interface Props {
  orderProductId: number
}

export default function OrderReviewCreatePage({ orderProductId }: Props) {
  return (
    <>
      <OrderReviewCreateHeader />
      <OrderReviewCreateContent orderProductId={orderProductId} />
    </>
  )
}
