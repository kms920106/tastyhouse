import SearchResultAllMenuList from './SearchResultAllMenuList'
import SearchResultAllSectionHeader from './SearchResultAllSectionHeader'

interface Props {
  query: string
}

export default function SearchResultAllMenuContent({ query }: Props) {
  return (
    <div className="px-[15px] py-[30px]">
      <SearchResultAllSectionHeader title="메뉴" />
      <SearchResultAllMenuList query={query} />
    </div>
  )
}
