import ClearCartOnMount from './ClearCartOnMount'
import OrderCompleteContent from './OrderCompleteContent'
import OrderCompleteHeader from './OrderCompleteHeader'

interface Props {
  orderId: number
}

export default async function OrderCompletePage({ orderId }: Props) {
  return (
    <>
      <ClearCartOnMount />
      <OrderCompleteHeader />
      <OrderCompleteContent orderId={orderId} />
    </>
  )
}
