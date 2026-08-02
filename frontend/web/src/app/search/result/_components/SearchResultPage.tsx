import type { SearchTab } from './SearchResultTabs'
import SearchResultHeader from './SearchResultHeader'
import SearchResultTabs from './SearchResultTabs'

interface Props {
  tab: SearchTab
  query: string
  isLoggedIn: boolean
}

export default function SearchResultPage({ tab, query, isLoggedIn }: Props) {
  return (
    <>
      <SearchResultHeader query={query} />
      <SearchResultTabs tab={tab} query={query} isLoggedIn={isLoggedIn} />
    </>
  )
}
