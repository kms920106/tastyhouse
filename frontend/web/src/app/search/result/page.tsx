import type { SearchTab } from './_components/SearchResultTabs'
import SearchResultPage from './_components/SearchResultPage'
import { getIsLoggedIn } from '@/lib/auth-config'

interface Props {
  searchParams: Promise<{
    q?: string
    tab?: string
  }>
}

const SEARCH_TAB_VALUES: SearchTab[] = ['all', 'menu', 'review', 'place']

function parseSearchTab(value: string | undefined): SearchTab {
  return SEARCH_TAB_VALUES.includes(value as SearchTab) ? (value as SearchTab) : 'all'
}

export default async function Page({ searchParams }: Props) {
  const [{ q, tab }, isLoggedIn] = await Promise.all([searchParams, getIsLoggedIn()])
  const initialTab = parseSearchTab(tab)

  return <SearchResultPage tab={initialTab} query={q ?? ''} isLoggedIn={isLoggedIn} />
}
