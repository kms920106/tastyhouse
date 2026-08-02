import ReservationContentClient from './ReservationContentClient'
import ReservationHeader from './ReservationHeader'

interface Props {
  shopId: number
}

export default function ReservationPage({ shopId }: Props) {
  return (
    <>
      <ReservationHeader />
      <ReservationContentClient shopId={shopId} />
    </>
  )
}
