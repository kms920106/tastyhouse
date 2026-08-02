import SearchResultMenuList from './SearchResultMenuList'

interface Props {
  query: string
}

export default function SearchResultMenuTabContent({ query }: Props) {
  return <SearchResultMenuList query={query} />
}
