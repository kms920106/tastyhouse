import EventPage from './_components/EventPage'
import type { EventTab } from './_components/EventTabs'

interface Props {
  searchParams: Promise<{
    tab?: string
  }>
}

const EVENT_TAB_VALUES: EventTab[] = ['ongoing', 'ended', 'winner']

function parseEventTab(value: string | undefined): EventTab {
  return EVENT_TAB_VALUES.includes(value as EventTab) ? (value as EventTab) : 'ongoing'
}

export default async function Page({ searchParams }: Props) {
  const { tab } = await searchParams
  const initialTab = parseEventTab(tab)

  return <EventPage tab={initialTab} />
}
