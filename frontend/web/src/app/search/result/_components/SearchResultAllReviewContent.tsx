import SearchResultAllReviewList from './SearchResultAllReviewList'
import SearchResultAllSectionHeader from './SearchResultAllSectionHeader'

interface Props {
  query: string
}

export default function SearchResultAllReviewContent({ query }: Props) {
  return (
    <div className="px-[15px] py-[30px]">
      <SearchResultAllSectionHeader title="리뷰" />
      <SearchResultAllReviewList query={query} />
    </div>
  )
}
