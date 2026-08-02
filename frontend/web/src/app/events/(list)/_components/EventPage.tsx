import EventTabs from './EventTabs'
import type { EventTab } from './EventTabs'
import EventHeader from './EventHeader'

interface Props {
  tab: EventTab
}

export default function EventPage({ tab }: Props) {
  return (
    <>
      <EventHeader />
      <EventTabs tab={tab} />
    </>
  )
}
