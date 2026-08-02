import OrderDetailContent from './OrderDetailContent'
import OrderDetailHeader from './OrderDetailHeader'

interface Props {
  orderId: number
}

export default function OrderDetailPage({ orderId }: Props) {
  return (
    <>
      <OrderDetailHeader />
      <OrderDetailContent orderId={orderId} />
    </>
  )
}
