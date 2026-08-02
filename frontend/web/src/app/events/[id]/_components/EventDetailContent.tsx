import { Suspense } from 'react'
import EventDetailImage from './EventDetailImage'
import EventDetailImageSkeleton from './EventDetailImageSkeleton'

interface Props {
  eventId: number
}

export default function EventDetailContent({ eventId }: Props) {
  return (
    <Suspense fallback={<EventDetailImageSkeleton />}>
      <EventDetailImage eventId={eventId} />
    </Suspense>
  )
}
