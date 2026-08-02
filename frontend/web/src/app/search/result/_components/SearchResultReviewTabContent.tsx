import SearchResultReviewList from './SearchResultReviewList'

interface Props {
  query: string
}

export default function SearchResultReviewTabContent({ query }: Props) {
  return <SearchResultReviewList query={query} />
}
