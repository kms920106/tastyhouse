import EventDetailPage from './_components/EventDetailPage'

interface Props {
  params: Promise<{
    id: string
  }>
}

export default async function Page({ params }: Props) {
  const { id } = await params
  const eventId = Number(id)

  return <EventDetailPage eventId={eventId} />
}
