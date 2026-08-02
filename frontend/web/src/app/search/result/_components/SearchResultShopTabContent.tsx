import SearchResultShopList from './SearchResultShopList'

interface Props {
  query: string
  isLoggedIn: boolean
}

export default function SearchResultShopTabContent({ query, isLoggedIn }: Props) {
  return <SearchResultShopList query={query} isLoggedIn={isLoggedIn} />
}
