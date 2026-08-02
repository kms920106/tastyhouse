import { Suspense } from 'react'
import ReservationCompleteContent from './ReservationCompleteContent'
import ReservationCompleteHeader from './ReservationCompleteHeader'
import ReservationCompleteSkeleton from './ReservationCompleteSkeleton'

interface Props {
  reservationId: number
}

export default function ReservationCompletePage({ reservationId }: Props) {
  return (
    <>
      <ReservationCompleteHeader />
      <Suspense fallback={<ReservationCompleteSkeleton />}>
        <ReservationCompleteContent reservationId={reservationId} />
      </Suspense>
    </>
  )
}
