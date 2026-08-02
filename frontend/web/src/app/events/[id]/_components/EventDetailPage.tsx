import EventDetailContent from './EventDetailContent'
import EventDetailHeader from './EventDetailHeader'

interface Props {
  eventId: number
}

export default async function EventDetailPage({ eventId }: Props) {
  return (
    <>
      <EventDetailHeader />
      <EventDetailContent eventId={eventId} />
    </>
  )
}
