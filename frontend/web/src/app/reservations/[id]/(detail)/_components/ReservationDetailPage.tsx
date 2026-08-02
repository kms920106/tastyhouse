import ReservationDetailContent from './ReservationDetailContent'
import ReservationDetailHeader from './ReservationDetailHeader'

interface Props {
  reservationId: number
}

export default function ReservationDetailPage({ reservationId }: Props) {
  return (
    <>
      <ReservationDetailHeader />
      <ReservationDetailContent reservationId={reservationId} />
    </>
  )
}
