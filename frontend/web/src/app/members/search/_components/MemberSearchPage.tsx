import MemberSearchHContent from './MemberSearchHContent'
import MemberSearchHeader from './MemberSearchHeader'

interface Props {
  query: string
}

export default function MemberSearchPage({ query }: Props) {
  return (
    <>
      <MemberSearchHeader />
      <MemberSearchHContent query={query} />
    </>
  )
}
