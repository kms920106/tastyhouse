import OrderMethodContent from './OrderMethodContent'
import OrderMethodHeader from './OrderMethodHeader'

interface Props {
  shopId: number
}

export default function OrderMethodPage({ shopId }: Props) {
  return (
    <>
      <OrderMethodHeader />
      <OrderMethodContent shopId={shopId} />
    </>
  )
}
