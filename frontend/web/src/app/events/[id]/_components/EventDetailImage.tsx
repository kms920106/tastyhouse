import FetchErrorState from '@/components/ui/FetchErrorState'
import { COMMON_ERROR_MESSAGES } from '@/constants/errors'
import { eventRepository } from '@/domains/event/event.repository'
import Image from 'next/image'

interface Props {
  eventId: number
}

export default async function EventDetailImage({ eventId }: Props) {
  const { error, status, data } = await eventRepository.getEventDetail(eventId)

  if ((error && status === 404) || !data) {
    return <FetchErrorState message={COMMON_ERROR_MESSAGES.FETCH_ERROR('이벤트')} />
  }

  if (error) {
    return <FetchErrorState message={COMMON_ERROR_MESSAGES.API_FETCH_ERROR} />
  }

  return (
    <Image
      src={data.bannerImageUrl}
      alt="이벤트 배너"
      width={1080}
      height={1436}
      className="w-full h-auto"
    />
  )
}
