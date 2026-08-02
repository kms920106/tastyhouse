import type { RankTab } from './_components/RankMemberTabs'
import RankPage from './_components/RankPage'

interface Props {
  searchParams: Promise<{
    tab?: string
  }>
}

const RANK_TAB_VALUES: RankTab[] = ['all', 'monthly']

function parseRankTab(value: string | undefined): RankTab {
  return RANK_TAB_VALUES.includes(value as RankTab) ? (value as RankTab) : 'all'
}

export default async function Page({ searchParams }: Props) {
  const { tab } = await searchParams
  const initialTab = parseRankTab(tab)

  return <RankPage tab={initialTab} />
}
