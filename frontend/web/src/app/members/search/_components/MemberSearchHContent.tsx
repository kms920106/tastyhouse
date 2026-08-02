import MemberSearchResultList from './MemberSearchResultList'

interface Props {
  query: string
}

export default function MemberSearchHContent({ query }: Props) {
  return (
    <div className="flex flex-col px-[15px]">
      <MemberSearchResultList query={query} />
    </div>
  )
}
