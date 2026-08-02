import SearchResultAllShopList from './SearchResultAllShopList'
import SearchResultAllSectionHeader from './SearchResultAllSectionHeader'

interface Props {
  query: string
  isLoggedIn: boolean
}

export default function SearchResultAllShopContent({ query, isLoggedIn }: Props) {
  return (
    <div className="px-[15px] py-[30px]">
      <SearchResultAllSectionHeader title="플레이스" />
      <SearchResultAllShopList query={query} isLoggedIn={isLoggedIn} />
    </div>
  )
}
